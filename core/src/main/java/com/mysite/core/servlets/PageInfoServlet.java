package com.mysite.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is version of servlet using ServletPath, it not recommend
 */
@Component(service = Servlet.class)
@SlingServletPaths(value = "/bin/practice/pageinfo")
public class PageInfoServlet extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(PageInfoServlet.class);

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @org.jetbrains.annotations.NotNull SlingHttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 1. read query param
        String pagePath = request.getParameter("path");
        if (pagePath == null || pagePath.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"error\": \"Missing 'path' query parameter.\"}");
            return;
        }

        // 2. Use ResourceResolver from request (this is resolver of logged-in user)
        // In servlet not need create new user resolver,  request.getResourceResolver.
        Resource pageResource = request.getResourceResolver().getResource(pagePath);

        if (pageResource == null) {
            response.setStatus(404);
            response.getWriter().write("{\"error\": \"Page not found at path: " + pagePath + "\"}");
        }

        // 3. read jcr:content node
        assert pageResource != null;
        Resource jcrContent = pageResource.getChild(com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT);
        if (jcrContent == null) {
            response.setStatus(404);

            response.getWriter().write("{\"error\": \"jcr:content node not found for); page at path: " + pagePath + "\"}");
            return;
        }

        // 4. read properties by ValueMap
        ValueMap props = jcrContent.getValueMap();
        String title = props.get("jcr:title", "No title");
        String resourceType = props.get("sling:resourceType", "Not Set");
        String lastModified = props.get("cq:lastModified", "N/A");

        // traversal child nodes of jcr:content
        List<String> childNames = new ArrayList<>();
        for (Resource child : jcrContent.getChildren()) {
            childNames.add(child.getName());
        }

        // build json response
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"path\": \"").append(pagePath).append("\",");
        json.append("\"title\": \"").append(title).append("\",");
        json.append("\"resourceType\": \"").append(resourceType).append("\",");
        json.append("\"lastModified\": \"").append(lastModified).append("\",");
        json.append("\"childNodes\": [");
        for (int i = 0; i < childNames.size(); i++) {
            json.append("\"").append(childNames.get(i)).append("\"");
            if (i < childNames.size() - 1) {
                json.append(",");
            }
        }
            json.append("]");
            json.append("}");

            log.debug("PageInfoServlet response for path: {}", pagePath);
            response.getWriter().write(json.toString());
    }
}
