package com.aido.backend.config;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class LoggingConfig {

    @Bean
    public ObjectMapper loggingObjectMapper() {
        return new ObjectMapper();
    }

    public static class StructuredMessageConverter extends MessageConverter {
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final Pattern keyValuePattern = Pattern.compile("(\\w+)=([^,\\s]+)");

        @Override
        public String convert(ILoggingEvent event) {
            String originalMessage = event.getFormattedMessage();
            
            try {
                ObjectNode messageObj = objectMapper.createObjectNode();
                
                // 기본 메시지 파싱 시도
                if (containsStructuredData(originalMessage)) {
                    parseStructuredMessage(originalMessage, messageObj);
                } else {
                    messageObj.put("text", originalMessage);
                }
                
                return objectMapper.writeValueAsString(messageObj);
            } catch (JsonProcessingException e) {
                // JSON 변환 실패시 원본 메시지 반환
                return originalMessage;
            }
        }

        private boolean containsStructuredData(String message) {
            return message.contains("=") && (message.contains(",") || message.contains(" "));
        }

        private void parseStructuredMessage(String message, ObjectNode messageObj) {
            // key=value 패턴 파싱
            Matcher matcher = keyValuePattern.matcher(message);
            boolean foundAny = false;
            
            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2);
                messageObj.put(key, value);
                foundAny = true;
            }
            
            if (foundAny) {
                // key=value가 있는 경우, 나머지 텍스트를 별도로 저장
                String remainingText = message.replaceAll("\\w+=\\S+[,\\s]*", "").trim();
                if (!remainingText.isEmpty()) {
                    messageObj.put("message", remainingText);
                }
            } else {
                messageObj.put("text", message);
            }
        }
    }
}