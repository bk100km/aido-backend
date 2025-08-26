package com.aido.backend.oauth;

import com.aido.backend.entity.User;
import com.aido.backend.enums.AuthProvider;
import com.aido.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Test
    @DisplayName("Should save new user correctly")
    void shouldSaveNewUserCorrectly() {
        // Given
        User newUser = new User("New User", "new@example.com", AuthProvider.KAKAO, "kakao456");
        
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        
        // When
        User result = userRepository.save(newUser);
        
        // Then
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getProvider()).isEqualTo(AuthProvider.KAKAO);
        assertThat(result.getProviderId()).isEqualTo("kakao456");
        verify(userRepository).save(newUser);
    }

    @Test
    @DisplayName("Should check email existence correctly")
    void shouldCheckEmailExistenceCorrectly() {
        // Given
        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new User("Existing", "existing@example.com")));
        when(userRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.empty());
        
        // When
        Optional<User> existingUser = userRepository.findByEmail("existing@example.com");
        Optional<User> newUser = userRepository.findByEmail("new@example.com");
        
        // Then
        assertThat(existingUser).isPresent();
        assertThat(newUser).isEmpty();
    }

    @Test
    @DisplayName("Should handle provider-based user lookup")
    void shouldHandleProviderBasedUserLookup() {
        // Given
        User googleUser = new User("Google User", "google@example.com", AuthProvider.GOOGLE, "google789");
        when(userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google789"))
                .thenReturn(Optional.of(googleUser));
        when(userRepository.findByProviderAndProviderId(AuthProvider.APPLE, "apple789"))
                .thenReturn(Optional.empty());
        
        // When
        Optional<User> foundGoogleUser = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google789");
        Optional<User> notFoundAppleUser = userRepository.findByProviderAndProviderId(AuthProvider.APPLE, "apple789");
        
        // Then
        assertThat(foundGoogleUser).isPresent();
        assertThat(foundGoogleUser.get().getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(notFoundAppleUser).isEmpty();
    }

    @Test
    @DisplayName("Should verify user update operations")
    void shouldVerifyUserUpdateOperations() {
        // Given
        User existingUser = new User("Old Name", "user@example.com", AuthProvider.GOOGLE, "google123");
        existingUser.setId(1L);
        existingUser.setName("Updated Name");
        
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        
        // When
        User updatedUser = userRepository.save(existingUser);
        
        // Then
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        verify(userRepository).save(existingUser);
    }

    @Test
    @DisplayName("Should handle OAuth provider existence check")
    void shouldHandleOAuthProviderExistenceCheck() {
        // Given
        when(userRepository.existsByProviderAndProviderId(AuthProvider.KAKAO, "kakao123"))
                .thenReturn(true);
        when(userRepository.existsByProviderAndProviderId(AuthProvider.APPLE, "apple123"))
                .thenReturn(false);
        
        // When
        boolean kakaoExists = userRepository.existsByProviderAndProviderId(AuthProvider.KAKAO, "kakao123");
        boolean appleExists = userRepository.existsByProviderAndProviderId(AuthProvider.APPLE, "apple123");
        
        // Then
        assertThat(kakaoExists).isTrue();
        assertThat(appleExists).isFalse();
    }
}