package com.mysite.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletResponse;

import java.io.IOException;

/**
 * Utility class for sending JSON responses in Sling servlets. Provides methods for sending both successful and error responses with proper JSON formatting.
 * @author thnhan1
 */
public final class JsonResponseUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private JsonResponseUtils() {}

    public static void ok(SlingHttpServletResponse resp, Object body) throws IOException {
        resp.setStatus(200);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(resp.getWriter(), body);
    }

    public static void error(SlingHttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(resp.getWriter(), body);
    }
}
