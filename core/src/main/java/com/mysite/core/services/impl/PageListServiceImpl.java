package com.mysite.core.services.impl;

import com.mysite.core.services.PageListService;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
@Component(service = PageListService.class)
public class PageListServiceImpl implements PageListService {
    private static final Logger log = LoggerFactory.getLogger(PageListServiceImpl.class);

    // Phải khớp với mapping trong ServiceUserMapper config bên dưới
    private static final String SUBSERVICE_NAME = "mysite-read-service";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public List<String> getChildPageTitles(String parentPath) {

        // validate input
        if (parentPath == null || parentPath.isEmpty()) {
            log.warn("parentPath is null or empty");
            return Collections.emptyList();
        }

        List<String> titles = new ArrayList<>();

        Map<String, Object> param = Collections.singletonMap(
                ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME
        );

        // try-with-resource - tự đóng resolver, tránh memory leak
        try (ResourceResolver resolver =
                resolverFactory.getServiceResourceResolver(param)) {
            Resource parent = resolver.getResource(parentPath);

            // 1. Null-check - path khong tồn tại trong JCR
            if (parent ==null) {
                log.warn("Resource not found at path: {}", parentPath);
                return Collections.emptyList();
            }

            // 2. Duyệt các node con trực tiếp (1 level)
            for (Resource child : parent.getChildren()) {

                // 3. Chỉ lấy node có primaryType là cq:Page
                String primaryType = child.getValueMap()
                        .get("jcr:primaryType", String.class);

                if (!"cq:Page".equals(primaryType)) {
                    continue;
                }

                // 4. Đọc jcr:content của page đó
                Resource jcrContent = child.getChild("jcr:content");
                if (jcrContent == null) {
                    log.debug("Page {} has no jcr:content, skipping", child.getPath());
                    continue;
                }

                // 5. Đọc jcr:title từ ValueMap
                ValueMap props = jcrContent.getValueMap();
                String title = props.get("jcr:title", child.getName()); // fallback lấy tên node nếu không có title

                log.debug("Found page: {} -> title: {}", child.getPath(), title);
                titles.add(title);
            }
        } catch (LoginException e) {
            log.error("Cannot obtain ResourceResolver with subservice '{}': {}",
                    SUBSERVICE_NAME, e.getMessage(), e);
        }
        return titles;
    }
}
