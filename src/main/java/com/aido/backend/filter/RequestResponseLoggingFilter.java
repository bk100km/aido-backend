package com.aido.backend.filter;

import com.aido.backend.util.LoggingUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        logRequest(requestWrapper);

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logResponse(responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("method", request.getMethod());
        requestInfo.put("uri", request.getRequestURI());
        
        if (request.getQueryString() != null) {
            requestInfo.put("query_string", request.getQueryString());
        }
        
        // 중요한 헤더만 로깅
        String userAgent = request.getHeader("User-Agent");
        String contentType = request.getHeader("Content-Type");
        String authorization = request.getHeader("Authorization");
        
        if (userAgent != null) requestInfo.put("user_agent", userAgent);
        if (contentType != null) requestInfo.put("content_type", contentType);
        if (authorization != null) requestInfo.put("has_auth", "true");
        
        String body = getStringValue(request.getContentAsByteArray(), request.getCharacterEncoding());
        if (!body.isEmpty() && body.length() < 1000) { // 큰 body는 제외
            requestInfo.put("body_preview", body.substring(0, Math.min(body.length(), 200)));
        }
        
        LoggingUtils.logRequest(logger, request.getMethod(), request.getRequestURI(), userAgent, requestInfo);
    }

    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        Map<String, Object> responseInfo = new HashMap<>();
        
        // 중요한 헤더만 로깅
        String contentType = response.getHeader("Content-Type");
        String location = response.getHeader("Location");
        
        if (contentType != null) responseInfo.put("content_type", contentType);
        if (location != null) responseInfo.put("location", location);
        
        String body = getStringValue(response.getContentAsByteArray(), response.getCharacterEncoding());
        if (!body.isEmpty() && body.length() < 1000) { // 큰 body는 제외
            responseInfo.put("body_preview", body.substring(0, Math.min(body.length(), 200)));
        }
        
        LoggingUtils.logResponse(logger, response.getStatus(), duration, responseInfo);
    }

    private String getStringValue(byte[] contentAsByteArray, String characterEncoding) {
        try {
            return new String(contentAsByteArray, characterEncoding != null ? characterEncoding : "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Unsupported encoding", e);
            return "";
        }
    }
}