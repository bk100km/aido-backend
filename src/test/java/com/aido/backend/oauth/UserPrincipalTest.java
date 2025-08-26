package com.aido.backend.oauth;

import com.aido.backend.entity.User;
import com.aido.backend.enums.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserPrincipalTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john@example.com");
        testUser.setId(1L);
        testUser.setProvider(AuthProvider.GOOGLE);
        testUser.setProviderId("google123");
    }

    @Test
    @DisplayName("Should create UserPrincipal from User")
    void shouldCreateUserPrincipalFromUser() {
        // When
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        // Then
        assertThat(userPrincipal.getId()).isEqualTo(1L);
        assertThat(userPrincipal.getEmail()).isEqualTo("john@example.com");
        assertThat(userPrincipal.getDisplayName()).isEqualTo("John Doe");
        assertThat(userPrincipal.getName()).isEqualTo("John Doe");
        assertThat(userPrincipal.getUsername()).isEqualTo("john@example.com");
        assertThat(userPrincipal.getAuthorities()).hasSize(1);
        assertThat(userPrincipal.getAuthorities()).contains(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    @DisplayName("Should create UserPrincipal with attributes")
    void shouldCreateUserPrincipalWithAttributes() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google123");
        attributes.put("name", "John Doe");
        attributes.put("email", "john@example.com");

        // When
        UserPrincipal userPrincipal = UserPrincipal.create(testUser, attributes);

        // Then
        assertThat(userPrincipal.getId()).isEqualTo(1L);
        assertThat(userPrincipal.getEmail()).isEqualTo("john@example.com");
        assertThat(userPrincipal.getAttributes()).isEqualTo(attributes);
    }

    @Test
    @DisplayName("Should return correct UserDetails properties")
    void shouldReturnCorrectUserDetailsProperties() {
        // When
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        // Then
        assertThat(userPrincipal.getPassword()).isNull();
        assertThat(userPrincipal.isAccountNonExpired()).isTrue();
        assertThat(userPrincipal.isAccountNonLocked()).isTrue();
        assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
        assertThat(userPrincipal.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should have ROLE_USER authority")
    void shouldHaveRoleUserAuthority() {
        // When
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        // Then
        assertThat(userPrincipal.getAuthorities()).isNotEmpty();
        GrantedAuthority authority = userPrincipal.getAuthorities().iterator().next();
        assertThat(authority.getAuthority()).isEqualTo("ROLE_USER");
    }
}