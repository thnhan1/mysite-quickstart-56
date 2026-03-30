package com.mysite.core.workflow;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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

    @Reference
    ResourceResolverFactory resolverFactory;

    @Override
    public void execute(WorkItem workItem,
                        WorkflowSession workflowSession,
                        MetaDataMap metaDataMap) throws WorkflowException {

        // 0. Validate payload type
        String payloadType = workItem.getWorkflowData().getPayloadType();
        if (!"JCR_PATH".equals(payloadType)) {
            log.warn("Unsupported payload type: {}.", payloadType);
            return;
        }

        // 1. get payload path + args
        String payloadPath = workItem.getWorkflowData().getPayload().toString();
        log.info("Processing payload: {}", payloadPath);
        String tagNamespace = metaDataMap.get("PROCESS_ARGS", "mysite:");

        // 3. Lay Session tu WorkflowSession
        Session session = workflowSession.adaptTo(Session.class);
        if (session == null) {
            throw new WorkflowException("Can not obtain JCR session");
        }

        // 4. Wrap ResourceResolver tu Sesison
        Map<String, Object> param = Collections.singletonMap("user.jcr.session",
                session);
        try (ResourceResolver resolver = resolverFactory.getResourceResolver(param)) {

            // 5. Get jcr:content resource
            Resource contentResource = resolver.getResource(payloadPath + "/jcr:content");
            if (contentResource == null) {
                log.warn("No jcr:content found at: {}", payloadPath);
                return;
            }

            // 6. Doc title
            ValueMap props = contentResource.getValueMap();
            String title = props.get("jcr:title", "");
            log.info("Page title: '{}' at {}", title, payloadPath);

            if (!title.toLowerCase().contains("news")) {
                log.info("Title does not contain 'news', skipping tagging for {}", payloadPath);
                return;
            }

            // 7. Dung TagManager de ghi tag
            TagManager tagManager = resolver.adaptTo(TagManager.class);
            if (tagManager == null) {
                throw new WorkflowException("Can not obtain TagManager");
            }

            String newTagId = tagNamespace + "content-type/news";
            Tag newTag = tagManager.resolve(newTagId);
            if (newTag == null) {
                log.warn("News tag not found at: {}", newTagId);
                return;
            }

            // Kiem tra tag da ton tai chua
            Tag[] existingTags = tagManager.getTags(contentResource);
            boolean alreadyTagged = Arrays.stream(existingTags)
                    .anyMatch(t -> t.getTagID().equals(newTagId));

            if (alreadyTagged) {
                log.info("Tag {} already exists, skipping", newTagId);
                return;
            }

            // Them tag moi vao danh sach
            Tag[] updatedTags = Arrays.copyOf(existingTags, existingTags.length + 1);
            updatedTags[existingTags.length] = newTag;
            tagManager.setTags(contentResource, updatedTags);
            log.info("Tagged {} with {}", payloadPath, newTagId);

            // 8. Save session
            session.save();
            log.info("Session saved for {}", payloadPath);
        } catch (LoginException e) {
            throw new WorkflowException("Cannot obtain ResourceResolver for: " + payloadPath, e);
        } catch (RepositoryException e) {
            throw new WorkflowException("Failed to save session for: " + payloadPath, e);
        }
    }
}
