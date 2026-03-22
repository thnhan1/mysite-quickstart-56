package com.mysite.core.servlets;

import com.mysite.core.utils.JsonResponseUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ví dụ 1: servlet resource type trả về page info động.
 * <a href="http://localhost:4502/content/mysite/us/en.pagedata.json">/content/mysite/en.pagedata.json</a> -u admin:admin
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
        resourceTypes = "cq:Page",
        selectors = "pagedata",
        extensions="json",
        methods = "GET"
)
public class PageDataServlet extends SlingSafeMethodsServlet {
    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws IOException {
       response.setContentType("application/json");
       response.setCharacterEncoding("UTF-8");
       // request.getResource() trả về chính resource mà URL đang trỏ
        // ở đây là cq:Page tại /content/mysite/us/en
        Resource page = request.getResource();
        Resource jcrContent = page.getChild("jcr:content");

        if (jcrContent == null) {
            response.setStatus(404);
            response.getWriter().write("{\"error\": \"jcr:content node not found for page at path: " + page.getPath() + "\"}");
            return;
        }

        ValueMap props = jcrContent.getValueMap();
        String title = props.get("jcr:title", "Untitle default value");
        String resType = props.get("sling:resourceType", "");
        List<String> childNames = new ArrayList<>();
        for (Resource child : jcrContent.getChildren()) {
            childNames.add(child.getName());
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", title);
        map.put("resType", resType);
        map.put("path", page.getPath());
        map.put("childNames", childNames);

        JsonResponseUtils.ok(response, map);


    }
}
