package com.mysite.core.servlets.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

@Component(service = Servlet.class)
@SlingServletPaths("/bin/mysite/read-jcr")
public class ReadJCRViaResourceResolverServiceServlet extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(ReadJCRViaResourceResolverServiceServlet.class);
    private static final String SUBSERVICE = "mysite-service-user";

    @Reference
    private ResourceResolverFactory resolverFactory;

//    @Reference
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            readWithServiceResolver(response);
        } catch (LoginException e) {
            log.error("Can not obtain service resolver{}", e.getMessage());
        } catch (IOException e) {
            log.error("Failed to write response {}", e.getMessage());
            response.sendError(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private List<Map<String, Object>> collectSubPagesV2(Resource resource) {
        List<Map<String, Object>> pages = new ArrayList<>();

        if (resource == null) return pages;

        resource.getChildren().forEach(child -> {
            if (!child.getName().equals("jcr:content")) {
                pages.add(Map.of("title", child.getName(), "path", child.getPath(), "index", UUID.randomUUID()));
            }
        });
        return pages;
    }

    private List<Map<String, Object>> collectSubPages(Resource resource) {
        List<Map<String, Object>> pages = new ArrayList<>();

        if (resource == null) return pages;

        int count = 1;
        for (Resource child : resource.getChildren()) {
            if (!child.getName().equals(com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT)) {
                pages.add(Map.of("title", child.getName(), "path", child.getPath(), "index", count++));

            }
        }

        return pages;
    }

    private void readWithServiceResolver(SlingHttpServletResponse response) throws LoginException, IOException {
        Map<String, Object> param = Collections.singletonMap(
                ResourceResolverFactory.SUBSERVICE, SUBSERVICE);

        try (ResourceResolver resolver = resolverFactory.getServiceResourceResolver(param)) {
            Resource pageResource = resolver.getResource("/content/mysite/us/en");
            List<Map<String, Object>> pages = collectSubPages(pageResource);
            objectMapper.writeValue(response.getWriter(), pages);
        }

    }

}
