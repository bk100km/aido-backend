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

    public static void logRequest(Logger logger, String method, String uri, String userAgent, Map<String, Object> additionalInfo) {
        try {
            ObjectNode logObject = objectMapper.createObjectNode();
            logObject.put("type", "http_request");
            logObject.put("method", method);
            logObject.put("uri", uri);
            logObject.put("user_agent", userAgent);
            logObject.put("timestamp", System.currentTimeMillis());
            
            if (additionalInfo != null) {
                additionalInfo.forEach((key, value) -> {
                    if (value != null) {
                        logObject.put(key, value.toString());
                    }
                });
            }
            
            logger.info(objectMapper.writeValueAsString(logObject));
        } catch (JsonProcessingException e) {
            logger.info("type=http_request, method={}, uri={}", method, uri);
        }
    }

    public static void logResponse(Logger logger, int status, long duration, Map<String, Object> additionalInfo) {
        try {
            ObjectNode logObject = objectMapper.createObjectNode();
            logObject.put("type", "http_response");
            logObject.put("status", status);
            logObject.put("duration_ms", duration);
            logObject.put("timestamp", System.currentTimeMillis());
            
            if (additionalInfo != null) {
                additionalInfo.forEach((key, value) -> {
                    if (value != null) {
                        logObject.put(key, value.toString());
                    }
                });
            }
            
            logger.info(objectMapper.writeValueAsString(logObject));
        } catch (JsonProcessingException e) {
            logger.info("type=http_response, status={}, duration_ms={}", status, duration);
        }
    }
}