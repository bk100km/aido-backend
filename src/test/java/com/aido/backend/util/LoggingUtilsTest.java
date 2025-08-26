package com.aido.backend.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingUtilsTest {

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(LoggingUtilsTest.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should log info with structured JSON format")
    void shouldLogInfoWithStructuredJsonFormat() throws Exception {
        // Given
        String action = "test_action";
        Map<String, Object> details = new HashMap<>();
        details.put("user_id", "123");
        details.put("operation", "login");

        // When
        LoggingUtils.logInfo(logger, action, details);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        
        JsonNode logJson = objectMapper.readTree(logEvent.getMessage());
        assertThat(logJson.get("action").asText()).isEqualTo("test_action");
        assertThat(logJson.get("level").asText()).isEqualTo("INFO");
        assertThat(logJson.get("user_id").asText()).isEqualTo("123");
        assertThat(logJson.get("operation").asText()).isEqualTo("login");
        assertThat(logJson.has("timestamp")).isTrue();
    }

    @Test
    @DisplayName("Should log error with structured JSON format")
    void shouldLogErrorWithStructuredJsonFormat() throws Exception {
        // Given
        String action = "test_error";
        String error = "Something went wrong";
        Map<String, Object> details = new HashMap<>();
        details.put("error_code", "500");

        // When
        LoggingUtils.logError(logger, action, error, details);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        
        JsonNode logJson = objectMapper.readTree(logEvent.getMessage());
        assertThat(logJson.get("action").asText()).isEqualTo("test_error");
        assertThat(logJson.get("level").asText()).isEqualTo("ERROR");
        assertThat(logJson.get("error").asText()).isEqualTo("Something went wrong");
        assertThat(logJson.get("error_code").asText()).isEqualTo("500");
    }

    @Test
    @DisplayName("Should log debug with structured JSON format")
    void shouldLogDebugWithStructuredJsonFormat() throws Exception {
        // Given
        logger.setLevel(ch.qos.logback.classic.Level.DEBUG);
        String action = "debug_action";
        Map<String, Object> details = new HashMap<>();
        details.put("debug_info", "test");

        // When
        LoggingUtils.logDebug(logger, action, details);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        
        JsonNode logJson = objectMapper.readTree(logEvent.getMessage());
        assertThat(logJson.get("action").asText()).isEqualTo("debug_action");
        assertThat(logJson.get("level").asText()).isEqualTo("DEBUG");
        assertThat(logJson.get("debug_info").asText()).isEqualTo("test");
    }

    @Test
    @DisplayName("Should log HTTP request with structured JSON format")
    void shouldLogHttpRequestWithStructuredJsonFormat() throws Exception {
        // Given
        String method = "GET";
        String uri = "/api/users";
        String userAgent = "Mozilla/5.0";
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("query_string", "page=1");

        // When
        LoggingUtils.logRequest(logger, method, uri, userAgent, additionalInfo);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        
        JsonNode logJson = objectMapper.readTree(logEvent.getMessage());
        assertThat(logJson.get("type").asText()).isEqualTo("http_request");
        assertThat(logJson.get("method").asText()).isEqualTo("GET");
        assertThat(logJson.get("uri").asText()).isEqualTo("/api/users");
        assertThat(logJson.get("user_agent").asText()).isEqualTo("Mozilla/5.0");
        assertThat(logJson.get("query_string").asText()).isEqualTo("page=1");
    }

    @Test
    @DisplayName("Should log HTTP response with structured JSON format")
    void shouldLogHttpResponseWithStructuredJsonFormat() throws Exception {
        // Given
        int status = 200;
        long duration = 150L;
        Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("content_type", "application/json");

        // When
        LoggingUtils.logResponse(logger, status, duration, additionalInfo);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        
        JsonNode logJson = objectMapper.readTree(logEvent.getMessage());
        assertThat(logJson.get("type").asText()).isEqualTo("http_response");
        assertThat(logJson.get("status").asInt()).isEqualTo(200);
        assertThat(logJson.get("duration_ms").asLong()).isEqualTo(150L);
        assertThat(logJson.get("content_type").asText()).isEqualTo("application/json");
    }

    @Test
    @DisplayName("Should handle null details gracefully")
    void shouldHandleNullDetailsGracefully() throws Exception {
        // Given
        String action = "test_action";

        // When
        LoggingUtils.logInfo(logger, action, null);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        
        JsonNode logJson = objectMapper.readTree(logEvent.getMessage());
        assertThat(logJson.get("action").asText()).isEqualTo("test_action");
        assertThat(logJson.get("level").asText()).isEqualTo("INFO");
        assertThat(logJson.has("timestamp")).isTrue();
    }

    @Test
    @DisplayName("Should handle null values in details map")
    void shouldHandleNullValuesInDetailsMap() throws Exception {
        // Given
        String action = "test_action";
        Map<String, Object> details = new HashMap<>();
        details.put("null_value", null);
        details.put("valid_value", "test");

        // When
        LoggingUtils.logInfo(logger, action, details);

        // Then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        
        JsonNode logJson = objectMapper.readTree(logEvent.getMessage());
        assertThat(logJson.get("action").asText()).isEqualTo("test_action");
        assertThat(logJson.get("valid_value").asText()).isEqualTo("test");
        assertThat(logJson.has("null_value")).isFalse();
    }
}