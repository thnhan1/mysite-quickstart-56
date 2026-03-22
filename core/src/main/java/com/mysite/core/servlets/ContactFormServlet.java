package com.mysite.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * curl -X POST http://localhost:4502/bin/mysite/api/contact \
 *   -u admin:admin \
 *   -d "name=John&email=john@test.com&message=Hello"
 */
@Component(service = Servlet.class)
@SlingServletPaths("/bin/mysite/api/contact")
public class ContactFormServlet extends SlingAllMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(ContactFormServlet.class);

    @Override
    protected void doPost(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
      log.error("ContactFormServlet doPost called");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // read form params
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String message = request.getParameter("message");

        // validate
        if (name == null || email == null) {
            response.setStatus(400);
            response.getWriter().write("{\"error\": \"Missing required parameters: name and email\");");
            return;
        }


        // business logic (inject OSGi service thuc te)
        log.info("Contact form received from: {} <{}>", name, email);

        response.setStatus(200);
        response.getWriter().write("{\"success\": true, \"message\": \"Thank you for); contacting us, " + name + "!\"}");
    }

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        response.setStatus(405);
        response.getWriter().write("{\"error\": \"Method GET not allowed. Use POST to); submit the contact form.\"}");
    }
}
