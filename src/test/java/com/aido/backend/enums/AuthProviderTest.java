package com.aido.backend.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthProviderTest {

    @Test
    @DisplayName("Should return correct values for each provider")
    void shouldReturnCorrectValuesForEachProvider() {
        assertThat(AuthProvider.LOCAL.getValue()).isEqualTo("local");
        assertThat(AuthProvider.GOOGLE.getValue()).isEqualTo("google");
        assertThat(AuthProvider.APPLE.getValue()).isEqualTo("apple");
        assertThat(AuthProvider.KAKAO.getValue()).isEqualTo("kakao");
    }

    @Test
    @DisplayName("Should create provider from string correctly")
    void shouldCreateProviderFromStringCorrectly() {
        assertThat(AuthProvider.fromString("local")).isEqualTo(AuthProvider.LOCAL);
        assertThat(AuthProvider.fromString("google")).isEqualTo(AuthProvider.GOOGLE);
        assertThat(AuthProvider.fromString("apple")).isEqualTo(AuthProvider.APPLE);
        assertThat(AuthProvider.fromString("kakao")).isEqualTo(AuthProvider.KAKAO);
    }

    @Test
    @DisplayName("Should be case insensitive when creating from string")
    void shouldBeCaseInsensitiveWhenCreatingFromString() {
        assertThat(AuthProvider.fromString("GOOGLE")).isEqualTo(AuthProvider.GOOGLE);
        assertThat(AuthProvider.fromString("Google")).isEqualTo(AuthProvider.GOOGLE);
        assertThat(AuthProvider.fromString("gOoGlE")).isEqualTo(AuthProvider.GOOGLE);
    }

    @Test
    @DisplayName("Should return LOCAL for unknown provider")
    void shouldReturnLocalForUnknownProvider() {
        assertThat(AuthProvider.fromString("facebook")).isEqualTo(AuthProvider.LOCAL);
        assertThat(AuthProvider.fromString("twitter")).isEqualTo(AuthProvider.LOCAL);
        assertThat(AuthProvider.fromString("unknown")).isEqualTo(AuthProvider.LOCAL);
        assertThat(AuthProvider.fromString(null)).isEqualTo(AuthProvider.LOCAL);
        assertThat(AuthProvider.fromString("")).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    @DisplayName("Should have all expected enum values")
    void shouldHaveAllExpectedEnumValues() {
        AuthProvider[] providers = AuthProvider.values();
        
        assertThat(providers).hasSize(4);
        assertThat(providers).containsExactlyInAnyOrder(
                AuthProvider.LOCAL,
                AuthProvider.GOOGLE,
                AuthProvider.APPLE,
                AuthProvider.KAKAO
        );
    }
}