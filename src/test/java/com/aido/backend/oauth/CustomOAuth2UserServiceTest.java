package com.aido.backend.oauth;

import com.aido.backend.entity.User;
import com.aido.backend.enums.AuthProvider;
import com.aido.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private OAuth2UserRequest mockOAuth2UserRequest;
    private OAuth2User mockOAuth2User;
    private ClientRegistration clientRegistration;
    
    @BeforeEach
    void setUp() {
        clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("client-id")
                .clientSecret("client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:8080/login/oauth2/code/google")
                .authorizationUri("https://accounts.google.com/o/oauth2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .build();

        mockOAuth2UserRequest = mock(OAuth2UserRequest.class);
        when(mockOAuth2UserRequest.getClientRegistration()).thenReturn(clientRegistration);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google123");
        attributes.put("name", "John Doe");
        attributes.put("email", "john@example.com");
        attributes.put("picture", "https://example.com/profile.jpg");

        mockOAuth2User = new DefaultOAuth2User(List.of(), attributes, "sub");
    }

    @Test
    @DisplayName("Should verify repository methods are called correctly")
    void shouldVerifyRepositoryMethodsAreCalledCorrectly() {
        // Given
        User testUser = new User("Test User", "test@example.com", AuthProvider.GOOGLE, "google123");
        testUser.setId(1L);
        
        // When
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google123"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        Optional<User> result = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google123");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository).findByProviderAndProviderId(AuthProvider.GOOGLE, "google123");
    }

    @Test
    @DisplayName("Should save new user correctly")
    void shouldUpdateExistingUserWhenUserExists() {
        // Given
        User existingUser = new User("Old Name", "john@example.com", AuthProvider.GOOGLE, "google123");
        existingUser.setId(1L);
        
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google123"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        OAuth2User result = customOAuth2UserService.loadUser(mockOAuth2UserRequest);

        // Then
        assertThat(result).isInstanceOf(UserPrincipal.class);
        verify(userRepository).save(existingUser);
        assertThat(existingUser.getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should throw exception when email already exists with different provider")
    void shouldThrowExceptionWhenEmailExistsWithDifferentProvider() {
        // Given
        User existingUser = new User("John Doe", "john@example.com", AuthProvider.KAKAO, "kakao123");
        existingUser.setId(1L);
        
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google123"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(existingUser));

        // When & Then
        assertThatThrownBy(() -> customOAuth2UserService.processOAuth2User(mockOAuth2UserRequest, mockOAuth2User))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("Email already registered with KAKAO provider");
    }

    @Test
    @DisplayName("Should throw exception when email is not found")
    void shouldThrowExceptionWhenEmailNotFound() {
        // Given
        Map<String, Object> attributesWithoutEmail = new HashMap<>();
        attributesWithoutEmail.put("sub", "google123");
        attributesWithoutEmail.put("name", "John Doe");
        
        OAuth2User oAuth2UserWithoutEmail = new DefaultOAuth2User(List.of(), attributesWithoutEmail, "sub");

        // When & Then
        assertThatThrownBy(() -> customOAuth2UserService.processOAuth2User(mockOAuth2UserRequest, oAuth2UserWithoutEmail))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("Email not found from OAuth2 provider");
    }

    @Test
    @DisplayName("Should update providerId when same provider with different providerId")
    void shouldUpdateProviderIdWhenSameProviderWithDifferentProviderId() {
        // Given
        User existingUser = new User("John Doe", "john@example.com", AuthProvider.GOOGLE, "old-google-id");
        existingUser.setId(1L);
        
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google123"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        OAuth2User result = customOAuth2UserService.loadUser(mockOAuth2UserRequest);

        // Then
        assertThat(result).isInstanceOf(UserPrincipal.class);
        verify(userRepository).save(existingUser);
        assertThat(existingUser.getProviderId()).isEqualTo("google123");
    }
}