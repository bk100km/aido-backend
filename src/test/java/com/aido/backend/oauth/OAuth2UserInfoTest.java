package com.aido.backend.oauth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2UserInfoTest {

    @Test
    @DisplayName("Should create Google OAuth2 user info correctly")
    void shouldCreateGoogleOAuth2UserInfo() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google123");
        attributes.put("name", "John Doe");
        attributes.put("email", "john@gmail.com");
        attributes.put("picture", "https://example.com/profile.jpg");

        // When
        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(attributes);

        // Then
        assertThat(userInfo.getId()).isEqualTo("google123");
        assertThat(userInfo.getName()).isEqualTo("John Doe");
        assertThat(userInfo.getEmail()).isEqualTo("john@gmail.com");
        assertThat(userInfo.getImageUrl()).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    @DisplayName("Should create Kakao OAuth2 user info correctly")
    void shouldCreateKakaoOAuth2UserInfo() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 123456789L);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("nickname", "카카오사용자");
        properties.put("profile_image", "https://kakao.com/profile.jpg");
        attributes.put("properties", properties);
        
        Map<String, Object> kakaoAccount = new HashMap<>();
        kakaoAccount.put("email", "user@kakao.com");
        attributes.put("kakao_account", kakaoAccount);

        // When
        KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(attributes);

        // Then
        assertThat(userInfo.getId()).isEqualTo("123456789");
        assertThat(userInfo.getName()).isEqualTo("카카오사용자");
        assertThat(userInfo.getEmail()).isEqualTo("user@kakao.com");
        assertThat(userInfo.getImageUrl()).isEqualTo("https://kakao.com/profile.jpg");
    }

    @Test
    @DisplayName("Should handle Kakao OAuth2 user info with null properties")
    void shouldHandleKakaoOAuth2UserInfoWithNullProperties() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", 123456789L);

        // When
        KakaoOAuth2UserInfo userInfo = new KakaoOAuth2UserInfo(attributes);

        // Then
        assertThat(userInfo.getId()).isEqualTo("123456789");
        assertThat(userInfo.getName()).isEqualTo("Kakao User");
        assertThat(userInfo.getEmail()).isNull();
        assertThat(userInfo.getImageUrl()).isNull();
    }

//    @Test
//    @DisplayName("Should create Apple OAuth2 user info correctly")
//    void shouldCreateAppleOAuth2UserInfo() {
//        // Given
//        Map<String, Object> attributes = new HashMap<>();
//        attributes.put("sub", "apple123");
//        attributes.put("email", "user@privaterelay.appleid.com");
//
//        Map<String, Object> nameMap = new HashMap<>();
//        nameMap.put("firstName", "John");
//        nameMap.put("lastName", "Doe");
//        attributes.put("name", nameMap);
//
//        // When
//        AppleOAuth2UserInfo userInfo = new AppleOAuth2UserInfo(attributes);
//
//        // Then
//        assertThat(userInfo.getId()).isEqualTo("apple123");
//        assertThat(userInfo.getName()).isEqualTo("John Doe");
//        assertThat(userInfo.getEmail()).isEqualTo("user@privaterelay.appleid.com");
//        assertThat(userInfo.getImageUrl()).isNull();
//    }

//    @Test
//    @DisplayName("Should handle Apple OAuth2 user info with no name")
//    void shouldHandleAppleOAuth2UserInfoWithNoName() {
//        // Given
//        Map<String, Object> attributes = new HashMap<>();
//        attributes.put("sub", "apple123");
//        attributes.put("email", "user@privaterelay.appleid.com");
//
//        // When
//        AppleOAuth2UserInfo userInfo = new AppleOAuth2UserInfo(attributes);
//
//        // Then
//        assertThat(userInfo.getId()).isEqualTo("apple123");
//        assertThat(userInfo.getName()).isEqualTo("Apple User");
//        assertThat(userInfo.getEmail()).isEqualTo("user@privaterelay.appleid.com");
//        assertThat(userInfo.getImageUrl()).isNull();
//    }
}