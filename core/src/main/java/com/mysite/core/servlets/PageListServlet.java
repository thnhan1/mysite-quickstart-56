// core/src/main/java/com/mysite/core/servlets/PageListServlet.java
package com.mysite.core.servlets;

import com.mysite.core.services.PageListService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/mysite/pagelist")
public class PageListServlet extends SlingSafeMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(PageListServlet.class);

    @Reference
    private PageListService pageListService;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String path = request.getParameter("path");

        if (path == null || path.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"Missing ?path= parameter\"}");
            return;
        }

        List<String> titles = pageListService.getChildPageTitles(path);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"path\":\"").append(path).append("\",");
        json.append("\"count\":").append(titles.size()).append(",");
        json.append("\"titles\":[");
        for (int i = 0; i < titles.size(); i++) {
            json.append("\"").append(titles.get(i)).append("\"");
            if (i < titles.size() - 1) json.append(",");
        }
        json.append("]}");

        response.getWriter().write(json.toString());
    }
}