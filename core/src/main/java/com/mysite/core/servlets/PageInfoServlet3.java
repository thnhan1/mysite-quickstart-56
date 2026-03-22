package com.mysite.core.servlets;

import com.mysite.core.utils.JsonResponseUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * This servlet test Resource API methods.
 * url : /content/mysite/ui/en.pageinfo2.json
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(resourceTypes = "cq:Page", // servlet only allow your page
        selectors = "pageinfo2", // selector to Trigger servlet
        extensions = "json", // ext
        methods = org.apache.sling.api.servlets.HttpConstants.METHOD_GET // Method
)
public class PageInfoServlet3 extends SlingSafeMethodsServlet {

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> map = new LinkedHashMap<>();

        Resource resource = request.getResource();

        if (resource == null) {
            response.setStatus(404);
            return;
        }

        map.put("path", resource.getPath()); // /content/mysite/us/en
        map.put("name", resource.getName());
        map.put("rsType", resource.getResourceType());
        map.put("valueMap", new HashMap<>(resource.getValueMap()));

        List<String> children = new ArrayList<>();
        for (Resource child : resource.getChildren()) {
            children.add(child.getPath());
        }

        map.put("children", children);
        Resource parent = resource.getParent();
        map.put("parent", parent != null ? parent.getPath() : null);

//        map.put("parent", resource.getParent()); // conetnt/mysite/us
        map.put("isPage", "cq:Page".equals(resource.getResourceType()));

        JsonResponseUtils.ok(response, map);
    }
}
