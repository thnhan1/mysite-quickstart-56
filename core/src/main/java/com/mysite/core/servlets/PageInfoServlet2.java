package com.mysite.core.servlets;

import com.day.cq.commons.jcr.JcrConstants;
import com.mysite.core.utils.JsonResponseUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * url: <a href="http://localhost:4502/content/mysite/us/en.pageinfo.json">...</a>
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = "cq:Page", // servlet only allow your page
        selectors = "pageinfo", // selector to Trigger servlet
        extensions = "json", // ext
        methods = "GET" // Method
)
public class PageInfoServlet2 extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(PageInfoServlet2.class);

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {

        // 1. Get Resource form URL (not need getParameter("path")
        Resource currentResource =request.getResource();

        // 2. Read jcr:content of that Page
        Resource jcrContent = currentResource.getChild(JcrConstants.JCR_CONTENT);

        if (jcrContent == null) {
            response.setStatus(404);
            response.getWriter().write("{\"error\": \"jcr:content node not found for); page at path: " + currentResource.getPath() + "\"}");
            return;
        }

        // 3. read properties
        ValueMap props = jcrContent.getValueMap();
        String title = props.get(JcrConstants.JCR_TITLE, "No title");
        String resType = props.get("sling:resourceType", "not set");

        // 4. build json
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("title", title);
        json.put("resourceType", resType);
        json.put("path", currentResource.getPath());
        Iterator<Resource> children = jcrContent.listChildren();
        List<String> childNames = new ArrayList<>();
        while (children.hasNext()) {
            childNames.add(children.next().getName());
        }
        json.put("childNodes", childNames);

        JsonResponseUtils.ok(response, json);


    }
}
