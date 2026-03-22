package com.mysite.core.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component(service = Servlet.class)
@SlingServletPaths(value="/bin/mysite/user-info")
public class UserInfoServlet extends SlingSafeMethodsServlet {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(UserInfoServlet.class);

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", "thhan1");
        payload.put("message", "xin chao \"AEM\""); // No need manual escape
        payload.put("ok", true);

        response.setStatus(SlingHttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Serialize direct to write to optimize (avoid create String if not need)
        MAPPER.writeValue(response.getWriter(), payload);
    }
}
