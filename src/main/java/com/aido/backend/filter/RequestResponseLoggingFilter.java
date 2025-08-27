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
import org.slf4j.MDC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

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

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 302 리다이렉션 체크
            if (isRedirection(responseWrapper.getStatus()) && isOAuthRelated(requestWrapper)) {
                logOAuthRedirection(requestWrapper, responseWrapper);
            }
            
            logOneLineApi(requestWrapper, responseWrapper, duration);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logOneLineApi(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        if (request.getQueryString() != null) {
            uri += "?" + request.getQueryString();
        }
        
        // Request headers 수집
        Map<String, String> requestHeaders = getImportantHeaders(request);
        
        // Request body 수집
        String requestBody = getStringValue(request.getContentAsByteArray(), request.getCharacterEncoding());
        if (requestBody.length() > 1000) {
            requestBody = requestBody.substring(0, 1000) + "...";
        }
        
        // Response headers 수집
        Map<String, String> responseHeaders = new HashMap<>();
        for (String headerName : response.getHeaderNames()) {
            if (isImportantHeader(headerName)) {
                responseHeaders.put(headerName, response.getHeader(headerName));
            }
        }
        
        // Response body 수집
        String responseBody = getStringValue(response.getContentAsByteArray(), response.getCharacterEncoding());
        if (responseBody.length() > 1000) {
            responseBody = responseBody.substring(0, 1000) + "...";
        }
        
        LoggingUtils.logOneLineApi(logger, method, uri, requestHeaders, requestBody, 
                                 response.getStatus(), responseHeaders, responseBody, duration);
    }
    
    private Map<String, String> getImportantHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (isImportantHeader(headerName)) {
                String headerValue = request.getHeader(headerName);
                // Authorization 헤더는 마스킹
                if ("Authorization".equalsIgnoreCase(headerName) && headerValue != null) {
                    headerValue = maskSensitiveData(headerValue);
                }
                headers.put(headerName, headerValue);
            }
        }
        return headers;
    }
    
    private boolean isImportantHeader(String headerName) {
        if (headerName == null) return false;
        String lowerName = headerName.toLowerCase();
        return lowerName.equals("content-type") || 
               lowerName.equals("authorization") || 
               lowerName.equals("user-agent") ||
               lowerName.equals("accept") ||
               lowerName.equals("accept-language") ||
               lowerName.equals("location") ||
               lowerName.equals("cache-control") ||
               lowerName.startsWith("x-");
    }
    
    private String maskSensitiveData(String value) {
        if (value == null || value.length() <= 8) {
            return "*****";
        }
        return value.substring(0, 4) + "*****" + value.substring(value.length() - 4);
    }
    
    private boolean isRedirection(int status) {
        return status >= 300 && status < 400;
    }
    
    private boolean isOAuthRelated(ContentCachingRequestWrapper request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        
        return uri.contains("/oauth") || 
               uri.contains("/login") || 
               uri.contains("/auth") ||
               (query != null && (query.contains("code=") || query.contains("state=")));
    }
    
    private void logOAuthRedirection(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        String method = request.getMethod();
        String fromUri = request.getRequestURI();
        if (request.getQueryString() != null) {
            fromUri += "?" + request.getQueryString();
        }
        
        String toLocation = response.getHeader("Location");
        String userAgent = request.getHeader("User-Agent");
        String traceId = MDC.get("traceId");
        
        LoggingUtils.logRedirection(logger, method, fromUri, toLocation, 
                                  response.getStatus(), userAgent, traceId);
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