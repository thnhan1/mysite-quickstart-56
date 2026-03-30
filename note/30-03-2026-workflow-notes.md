## Workflow Session không save xuong jcr dù log ra được

```java
package com.mysite.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Thêm cq:tags vào page
 */
@Component(
        service = WorkflowProcess.class,
        property = {
                "process.label=My Site - Auto Tag Pages"
        }
)
public class AutoTagProcessStep implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(AutoTagProcessStep.class);

    @Override
    public void execute(WorkItem workItem,
                        WorkflowSession workflowSession,
                        MetaDataMap metaDataMap) throws WorkflowException {
        // 1. get payload path
        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        log.info("Processing payload: {}", payloadPath);

        // 2. Read process step args (configured in WK model editor)
        String tagNamespace = metaDataMap.get("PROCESS_ARGS", "mysite:");

        // 3. Access JCR bang workflow session's resource resolver.
        ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        if (resolver == null) {
            throw new WorkflowException("Cound not obtain ResourceResolver");
        }

        // get jcr:content Resource
        Resource contentResource = resolver.getResource(payloadPath +"/jcr:content");
        if (contentResource == null) {
            log.warn("No jcr:content found at {}", payloadPath);
            return;
        }

        // 4. Ap dung business logic
        ModifiableValueMap properties = contentResource.adaptTo(ModifiableValueMap.class);
        if (properties != null) {
            String title = properties.get("jcr:title", "");
            // e.g. auto-tags based title keywords
            if (title.toLowerCase().contains("news")) {
                String [] existingTags = properties.get("cq:tags", new String[0]);
                String newTag = tagNamespace + "content-type/news";
                if (!Arrays.asList(existingTags).contains(newTag)) {
                    String[] updatedTags = Arrays.copyOf(
                            existingTags, existingTags.length+1
                    );
                    updatedTags[existingTags.length] = newTag;
                    properties.put("cq:tags", updatedTags);
                    // Rat tiec no se khong luu neu khong tat Tagging Validation Config
                    log.info("Tagged {} voi {}", payloadPath, newTag);
                }
            }
            resolver.commit();
        }
    }
}

```

1. `resolver.commit()` khong ghi duoc trong Workflow context

- Trogn AEM 6.5 `workflowSession.adaptTo(ResourceResolver.class)` tra về resolver read-only. không throw ex nhưng ko ghi xuống JCR.

Fix: lấy session từ `workflowSession` rồi wrap thành `ResourceResolver`

```java
    Session session = workflowSession.adaptTo(Session.class);
    Map<String, Object> param = Collections.singletonMap("user.jcr.session", session);
    ResourceResolver resolver = resolverFactory.getResourceResolver(param);
    // ...
    session.save(); // thay vì resolver.commit()
```

2. Ghi `cq:tags` bằng Strign array không đáng tin.

AEM Validate tag id qua `TagManager` - ghi thẳng Strign có thể bị strip hoặc không resolve được trên ui.

Fix dùng TagManager API:

```java
    TagManager tagManager = resolver.adaptTo(TagManager.class);
    Tag tag = tagManager.resolve(newTagId);
    if (tag == null) {
        log.warn("Tag does not exist: {}", newTagId);
        return;
    }
    tagManager.setTags(contentResource, updatedTags);
```

3. Không Validate payload type

Workflow có thể được trigger với payload không phải JCR path.

fix:

```java
String payloadType = workItem.getWorkflowData().getPayloadType();
if (!"JCR_PATH".equals(payloadType)) {
    log.warn("Unsupported payload type: {}", payloadType);
    return;
}
```

4. 🟡 resolver.commit() nằm trong if (properties != null)
Gọi commit kể cả khi không có gì thay đổi — lãng phí và không rõ ràng về intent.
Fix: Dùng flag changed và commit ngoài block logic:

```java
javaboolean changed = false;
// ... logic set changed = true khi cần
if (changed) {
    session.save();
}
```

5. Final version

[AutoTagProcessStep.java](core/src/main/java/com/mysite/core/workflow/AutoTagProcessStep.java)



### AI Khuyên

- Với `AutoTagProcessStep — tag mysite:content-type/news` là tag cố định, nên tạo thủ công 1 lần trong Tag Manager UI rồi để workflow apply. Không nên để workflow tự tạo tag vì dễ sinh tag rác nếu tagNamespace bị cấu hình sai.

```java
Tag newTag = tagManager.resolve(newTagId);
if (newTag == null) {
    log.info("Tag chua ton tai, tien hanh tao: {}", newTagId);
    try {
        newTag = tagManager.createTag(newTagId, "News", "Auto created tag");
        session.save(); // save sau khi tao tag
        log.info("Da tao tag: {}", newTagId);
    } catch (Exception e) {
        throw new WorkflowException("Cannot create tag: " + newTagId, e);
    }
}
```



### Tao Tag moi the nao

Chưa đủ — tạo tag tên "News" thôi chưa đủ, cần đúng cấu trúc namespace.
tagManager.resolve("mysite:content-type/news") tìm tag theo Tag ID, không phải tên hiển thị. Tag ID được xác định bởi cấu trúc node trong JCR:

```
/content/cq:tags/
    └── mysite/              ← namespace (Tag ID: mysite:)
            └── content-type/        ← (Tag ID: mysite:content-type)
                    └── news         ← (Tag ID: mysite:content-type/news)
```

#### Cách tạo đúng trong Tag Manager UI
[http://localhost:4502/libs/cq/tagging/gui/content/tags.html](http://localhost:4502/libs/cq/tagging/gui/content/tags.html)

Bước 1 — Tạo namespace:
Create Namespace
  Title: My Site
  Name:  mysite        ← bắt buộc phải là "mysite"

Bước 2 — Tạo tag con:

```
Vào namespace mysite → Create Tag
  Title: Content Type
  Name:  content-type

Vào content-type → Create Tag
  Title: News
  Name:  news          ← bắt buộc phải là "news"
```

Sau khi tạo xong, verify trong CRX/DE:

```
/content/cq:tags/mysite/content-type/news  ← node này phải tồn tại
```
Lúc đó tagManager.resolve("mysite:content-type/news") mới trả về tag thay vì null.




