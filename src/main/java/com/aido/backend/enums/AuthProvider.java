package com.aido.backend.enums;

public enum AuthProvider {
    LOCAL("local"),
    GOOGLE("google"),
    APPLE("apple"),
    KAKAO("kakao");

    private final String value;

    AuthProvider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AuthProvider fromString(String text) {
        for (AuthProvider provider : AuthProvider.values()) {
            if (provider.value.equalsIgnoreCase(text)) {
                return provider;
            }
        }
        return LOCAL;
    }
}