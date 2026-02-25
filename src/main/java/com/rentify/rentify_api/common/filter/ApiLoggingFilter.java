package com.rentify.rentify_api.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final int CONTENT_CACHE_LIMIT = 1024 * 1024;
    private static final List<String> EXCLUDE_PATHS = List.of(
        "/swagger-ui",
        "/api-docs",
        "/v3/api-docs",
        "/swagger-resources",
        "/webjars"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDE_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        if (request.getContentType() != null &&
            request.getContentType().startsWith("multipart/form-data")) {
            filterChain.doFilter(request, response);
        }

        ContentCachingRequestWrapper cachingRequest =
            new ContentCachingRequestWrapper(request, CONTENT_CACHE_LIMIT);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(cachingRequest, cachingResponse);
        } finally {
            long timeTaken = System.currentTimeMillis() - startTime;
            logRequestDetails(cachingRequest, timeTaken);
            cachingResponse.copyBodyToResponse();
        }
    }

    private void logRequestDetails(ContentCachingRequestWrapper request, long timeTaken) {
        String clientIp = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString() != null ? "?" + request.getQueryString() : "";

        byte[] content = request.getContentAsByteArray();
        String requestBody = content.length > 0 ? new String(content, StandardCharsets.UTF_8)
            .replaceAll("\\s+", " ") : "Empty Body";

        requestBody = maskSensitiveData(requestBody);

        log.info("[API Request] ClientIP: {} | Endpoint: {}{} | Time: {}ms | Body: {}",
            clientIp, method, uri + queryString, timeTaken, requestBody);
    }

    private String maskSensitiveData(String body) {
        if (body == null || "Empty Body".equals(body)) {
            return body;
        }
        return body.replaceAll("(\"(?i)[^\"]*password[^\"]*\"\\s*:\\s*\")[^\"]+(\")", "$1[PROTECTED]$2");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return (ip != null && ip.contains(",")) ? ip.split(",")[0] : ip;
    }
}
