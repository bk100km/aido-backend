package com.aido.backend.oauth;

import com.aido.backend.enums.AuthProvider;
import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId == null || attributes == null) {
            throw new IllegalArgumentException("Registration ID and attributes cannot be null");
        }
        
        AuthProvider provider = AuthProvider.fromString(registrationId);
        
        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case APPLE -> new AppleOAuth2UserInfo(attributes);
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("Sorry! Login with " + registrationId + " is not supported yet.");
        };
    }
}