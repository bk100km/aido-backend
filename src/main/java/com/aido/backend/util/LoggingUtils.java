package com.aido.backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;

import java.util.Map;

public class LoggingUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void logInfo(Logger logger, String action, Map<String, Object> details) {
        try {
            ObjectNode logObject = objectMapper.createObjectNode();
            logObject.put("action", action);
            logObject.put("level", "INFO");
            logObject.put("timestamp", System.currentTimeMillis());
            
            if (details != null) {
                details.forEach((key, value) -> {
                    if (value != null) {
                        logObject.put(key, value.toString());
                    }
                });
            }
            
            logger.info(objectMapper.writeValueAsString(logObject));
        } catch (JsonProcessingException e) {
            logger.info("action={}, error={}", action, e.getMessage());
        }
    }

    public static void logError(Logger logger, String action, String error, Map<String, Object> details) {
        try {
            ObjectNode logObject = objectMapper.createObjectNode();
            logObject.put("action", action);
            logObject.put("level", "ERROR");
            logObject.put("error", error);
            logObject.put("timestamp", System.currentTimeMillis());
            
            if (details != null) {
                details.forEach((key, value) -> {
                    if (value != null) {
                        logObject.put(key, value.toString());
                    }
                });
            }
            
            logger.error(objectMapper.writeValueAsString(logObject));
        } catch (JsonProcessingException e) {
            logger.error("action={}, error={}", action, error);
        }
    }

    public static void logDebug(Logger logger, String action, Map<String, Object> details) {
        try {
            ObjectNode logObject = objectMapper.createObjectNode();
            logObject.put("action", action);
            logObject.put("level", "DEBUG");
            logObject.put("timestamp", System.currentTimeMillis());
            
            if (details != null) {
                details.forEach((key, value) -> {
                    if (value != null) {
                        logObject.put(key, value.toString());
                    }
                });
            }
            
            logger.debug(objectMapper.writeValueAsString(logObject));
        } catch (JsonProcessingException e) {
            logger.debug("action={}, details={}", action, details);
        }
    }

    public static void logOneLineApi(Logger logger, String method, String uri, Map<String, String> requestHeaders, 
                                     String requestBody, int status, Map<String, String> responseHeaders, String responseBody, 
                                     long duration) {
        try {
            ObjectNode logObject = objectMapper.createObjectNode();
            logObject.put("type", "One line log");
            logObject.put("method", method);
            logObject.put("uri", uri);
            logObject.put("status", status);
            logObject.put("duration_ms", duration);
            logObject.put("timestamp", System.currentTimeMillis());
            
            // Request 정보
            ObjectNode requestNode = objectMapper.createObjectNode();
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                ObjectNode reqHeadersNode = objectMapper.createObjectNode();
                requestHeaders.forEach(reqHeadersNode::put);
                requestNode.set("headers", reqHeadersNode);
            }
            if (requestBody != null && !requestBody.trim().isEmpty()) {
                requestNode.put("body", requestBody);
            }
            if (requestNode.size() > 0) {
                logObject.set("request", requestNode);
            }
            
            // Response 정보
            ObjectNode responseNode = objectMapper.createObjectNode();
            if (responseHeaders != null && !responseHeaders.isEmpty()) {
                ObjectNode resHeadersNode = objectMapper.createObjectNode();
                responseHeaders.forEach(resHeadersNode::put);
                responseNode.set("headers", resHeadersNode);
            }
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                responseNode.put("body", responseBody);
            }
            if (responseNode.size() > 0) {
                logObject.set("response", responseNode);
            }
            
            logger.info(objectMapper.writeValueAsString(logObject));
        } catch (JsonProcessingException e) {
            logger.info("One line log: {} {} - {} ({}ms)", method, uri, status, duration);
        }
    }
    
    public static void logOneLineClientApi(Logger logger, String method, String url, Map<String, String> requestHeaders, 
                                           String requestBody, int status, Map<String, String> responseHeaders, 
                                           String responseBody, long duration, String target) {
        try {
            ObjectNode logObject = objectMapper.createObjectNode();
            logObject.put("type", "Client One line log");
            logObject.put("target", target);
            logObject.put("method", method);
            logObject.put("url", url);
            logObject.put("status", status);
            logObject.put("duration_ms", duration);
            logObject.put("timestamp", System.currentTimeMillis());
            
            // Request 정보
            ObjectNode requestNode = objectMapper.createObjectNode();
            if (requestHeaders != null && !requestHeaders.isEmpty()) {
                ObjectNode reqHeadersNode = objectMapper.createObjectNode();
                requestHeaders.forEach(reqHeadersNode::put);
                requestNode.set("headers", reqHeadersNode);
            }
            if (requestBody != null && !requestBody.trim().isEmpty()) {
                requestNode.put("body", requestBody);
            }
            if (requestNode.size() > 0) {
                logObject.set("request", requestNode);
            }
            
            // Response 정보
            ObjectNode responseNode = objectMapper.createObjectNode();
            if (responseHeaders != null && !responseHeaders.isEmpty()) {
                ObjectNode resHeadersNode = objectMapper.createObjectNode();
                responseHeaders.forEach(resHeadersNode::put);
                responseNode.set("headers", resHeadersNode);
            }
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                responseNode.put("body", responseBody);
            }
            if (responseNode.size() > 0) {
                logObject.set("response", responseNode);
            }
            
            logger.info(objectMapper.writeValueAsString(logObject));
        } catch (JsonProcessingException e) {
            logger.info("Client One line log: {} {} {} - {} ({}ms)", target, method, url, status, duration);
        }
    }
    
    public static void logRedirection(Logger logger, String method, String fromUri, String toLocation, 
                                     int status, String userAgent, String traceId) {
        try {
            ObjectNode logObject = objectMapper.createObjectNode();
            logObject.put("type", "OAuth Redirection log");
            logObject.put("method", method);
            logObject.put("from_uri", fromUri);
            logObject.put("to_location", maskRedirectUrl(toLocation));
            logObject.put("status", status);
            logObject.put("timestamp", System.currentTimeMillis());
            
            if (userAgent != null) {
                logObject.put("user_agent", userAgent);
            }
            
            if (traceId != null && !traceId.trim().isEmpty()) {
                logObject.put("trace_id", traceId);
            }
            
            // OAuth 플로우 단계 추출
            String oauthStep = extractOAuthStep(fromUri, toLocation);
            if (oauthStep != null) {
                logObject.put("oauth_step", oauthStep);
            }
            
            logger.info(objectMapper.writeValueAsString(logObject));
        } catch (JsonProcessingException e) {
            logger.info("OAuth Redirection: {} {} -> {} ({})", method, fromUri, maskRedirectUrl(toLocation), status);
        }
    }
    
    private static String maskRedirectUrl(String url) {
        if (url == null) return null;
        
        // OAuth 관련 민감한 파라미터들 마스킹
        return url.replaceAll("(code=)[^&]*", "$1*****")
                  .replaceAll("(access_token=)[^&]*", "$1*****") 
                  .replaceAll("(refresh_token=)[^&]*", "$1*****")
                  .replaceAll("(client_secret=)[^&]*", "$1*****")
                  .replaceAll("(state=)[^&]*", "$1*****");
    }
    
    private static String extractOAuthStep(String fromUri, String toLocation) {
        if (fromUri == null) return null;
        
        if (fromUri.contains("/oauth2/authorization/")) {
            return "oauth_initiation";
        } else if (fromUri.contains("/login/oauth2/code/")) {
            return "oauth_callback";
        } else if (toLocation != null && toLocation.contains("oauth")) {
            return "oauth_provider_redirect";
        }
        
        return null;
    }
}