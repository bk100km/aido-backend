package com.aido.backend.web;

import org.springframework.stereotype.Service;

@Service
public class OAuthAvailabilityService {
    
    public boolean isGoogleAvailable() {
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
        return clientId != null && !clientId.equals("disabled") && 
               clientSecret != null && !clientSecret.equals("disabled");
    }
    
    public boolean isKakaoAvailable() {
        String clientId = System.getenv("KAKAO_CLIENT_ID");
        String clientSecret = System.getenv("KAKAO_CLIENT_SECRET");
        return clientId != null && !clientId.equals("disabled") && 
               clientSecret != null && !clientSecret.equals("disabled");
    }
    
    public boolean isAppleAvailable() {
        String clientId = System.getenv("APPLE_CLIENT_ID");
        String clientSecret = System.getenv("APPLE_CLIENT_SECRET");
        return clientId != null && !clientId.equals("disabled") && 
               clientSecret != null && !clientSecret.equals("disabled");
    }
    
    public boolean isAnyOAuthAvailable() {
        return isGoogleAvailable() || isKakaoAvailable() || isAppleAvailable();
    }
}