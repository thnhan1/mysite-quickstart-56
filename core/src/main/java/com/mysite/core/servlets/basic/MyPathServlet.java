package com.mysite.core.servlets.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.core.utils.dto.ApiResponse;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Demo Servlet path, get method
 */
@Component(service = {Servlet.class},
        property = {Constants.SERVICE_DESCRIPTION
                + "=Simple Path Servlet", "sling.servlet.methods=GET", // define method
        })
@SlingServletPaths(value = "/bin/ping")
public class MyPathServlet extends SlingSafeMethodsServlet {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(MyPathServlet.class);

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        log.info("Nhan Reach bin/ping");
        MAPPER.writeValue(response.getWriter(), new ApiResponse<>("Ok", "Pong"));
    }
}
