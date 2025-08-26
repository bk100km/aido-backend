package com.aido.backend.oauth;

import java.util.Map;

public class AppleOAuth2UserInfo extends OAuth2UserInfo {

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        // Apple doesn't always provide name in the token
        // Name is usually provided only during the first authentication
        Object nameObj = attributes.get("name");
        if (nameObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> nameMap = (Map<String, Object>) nameObj;
            String firstName = (String) nameMap.get("firstName");
            String lastName = (String) nameMap.get("lastName");
            String fullName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
            return fullName.trim().isEmpty() ? "Apple User" : fullName.trim();
        }
        String name = (String) nameObj;
        return name != null && !name.trim().isEmpty() ? name : "Apple User";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        // Apple doesn't provide profile image
        return null;
    }
}