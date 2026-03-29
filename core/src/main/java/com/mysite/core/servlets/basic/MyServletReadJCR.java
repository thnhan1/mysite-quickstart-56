package com.mysite.core.servlets.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

/**
 * 3 method to read jcr from Sling Servlet Path (not recommend in prod)
 */
@Component(service = Servlet.class)
@SlingServletPaths("/bin/read-data")
public class MyServletReadJCR extends SlingSafeMethodsServlet {
    private static final Logger log = LoggerFactory.getLogger(MyServletReadJCR.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        ResourceResolver resolver = request.getResourceResolver();
        Resource resource = resolver.getResource("/content/mysite/us/en");

        String path = StringUtils.defaultIfBlank(request.getParameter("v"), "1");

        try {

            switch (path) {
                case "1":
                    readByRequestResourceResolver(request, response, resource);
                    break;
                case "2":
                    readByRequestResourceResolver2(request, response, resource);
                    break;
                case "3":
                    readByRequestResourceResolver3(request, response, resource);
                    break;
                default:
                    readByRequestResourceResolver(request, response, resource);
                    break;
            }
        } catch (Exception e) {
            log.error("Error while reading resource resolver {}", e.getMessage());
        }


    }

    /**
     * 1. Read JCR by Resource Resolver (not recommend in prod)
     * Read data publicly
     *
     * @param request
     * @param response
     */
    private void readByRequestResourceResolver(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response, Resource pageResource)
            throws IOException {
        List<Map<String, Object>> pages = new ArrayList<>();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        int count = 1;
        if (pageResource != null) {
            for (Resource subPage : pageResource.getChildren()) {
                // subPage se la nut cq:Page con hoac nut jcr:content
                if (!subPage.getName().equals("jcr:content")) {
                    Map<String, Object> subPageInfo = new HashMap<>();
                    subPageInfo.put("title", subPage.getName());
                    subPageInfo.put("path", subPage.getPath());
                    subPageInfo.put("index", count);
                    count += 1;

                    pages.add(subPageInfo);
                }
            }
        }

        mapper.writeValue(response.getWriter(), pages);
    }

    private void readByRequestResourceResolver3(
            SlingHttpServletRequest req,
            @NotNull SlingHttpServletResponse res,
            Resource resource
    ) throws IOException {
        Iterator<Resource> children = resource.getChildren().iterator();
        while (children.hasNext()) {
            Resource child = children.next();
            if (!child.getName().equals("jcr:content")) {
                // get jcr:content of page con
                log.error("ten page {}", child.getChild(com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT).getValueMap().get(com.day.cq.commons.jcr.JcrConstants.JCR_TITLE, String.class));
            }

        }
        log.debug("Done");
    }

    private void readByRequestResourceResolver2(SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response, Resource pageResource) throws IOException {
        List<Map<String, Object>> pages = new ArrayList<>();

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        int count = 1;
        if (pageResource != null) {
            pageResource.getChildren().forEach(subPage -> {
                if (!subPage.getName().equals(com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT)) {
                    pages.add(Map.of("title", subPage.getName(), "path", subPage.getPath(), "index", UUID.randomUUID()));
                }
            });
        }
        mapper.writeValue(response.getWriter(), pages);
    }
}
