package com.aido.backend.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OAuth2UserInfoFactoryTest {

    @Test
    @DisplayName("Should create Google OAuth2 user info")
    void shouldCreateGoogleOAuth2UserInfo() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google123");
        attributes.put("name", "John Doe");
        attributes.put("email", "john@gmail.com");

        // When
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google", attributes);

        // Then
        assertThat(userInfo).isInstanceOf(GoogleOAuth2UserInfo.class);
        assertThat(userInfo.getId()).isEqualTo("google123");
        assertThat(userInfo.getName()).isEqualTo("John Doe");
        assertThat(userInfo.getEmail()).isEqualTo("john@gmail.com");
    }

//    @Test
//    @DisplayName("Should create Apple OAuth2 user info")
//    void shouldCreateAppleOAuth2UserInfo() {
//        // Given
//        Map<String, Object> attributes = new HashMap<>();
//        attributes.put("sub", "apple123");
//        attributes.put("email", "user@privaterelay.appleid.com");
//
//        // When
//        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("apple", attributes);
//
//        // Then
//        assertThat(userInfo).isInstanceOf(AppleOAuth2UserInfo.class);
//        assertThat(userInfo.getId()).isEqualTo("apple123");
//        assertThat(userInfo.getEmail()).isEqualTo("user@privaterelay.appleid.com");
//    }

    @Test
    @DisplayName("Should create Kakao OAuth2 user info")
    void shouldCreateKakaoOAuth2UserInfo() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 123456789L);

        // When
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("kakao", attributes);

        // Then
        assertThat(userInfo).isInstanceOf(KakaoOAuth2UserInfo.class);
        assertThat(userInfo.getId()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("Should throw exception for unsupported provider")
    void shouldThrowExceptionForUnsupportedProvider() {
        // Given
        Map<String, Object> attributes = new HashMap<>();

        // When & Then
        assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo("facebook", attributes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sorry! Login with facebook is not supported yet.");
    }

    @Test
    @DisplayName("Should throw exception for null registration ID")
    void shouldThrowExceptionForNullRegistrationId() {
        // Given
        Map<String, Object> attributes = new HashMap<>();

        // When & Then
        assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo(null, attributes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Registration ID and attributes cannot be null");
    }

    @Test
    @DisplayName("Should throw exception for null attributes")
    void shouldThrowExceptionForNullAttributes() {
        // When & Then
        assertThatThrownBy(() -> OAuth2UserInfoFactory.getOAuth2UserInfo("google", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Registration ID and attributes cannot be null");
    }
}