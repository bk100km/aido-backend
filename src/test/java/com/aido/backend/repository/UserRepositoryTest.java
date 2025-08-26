package com.aido.backend.repository;

import com.aido.backend.entity.User;
import com.aido.backend.enums.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = new User("John Doe", "john.doe@example.com");
        testUser2 = new User("Jane Smith", "jane.smith@example.com");
    }

    @Test
    @DisplayName("Should save and find user by ID")
    void shouldSaveAndFindUserById() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser1);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
        assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        // Given
        entityManager.persistAndFlush(testUser1);

        // When
        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    void shouldReturnEmptyWhenUserNotFoundByEmail() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find users by name containing ignore case")
    void shouldFindUsersByNameContainingIgnoreCase() {
        // Given
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        // When
        List<User> foundUsers = userRepository.findByNameContainingIgnoreCase("john");

        // Then
        assertThat(foundUsers).hasSize(1);
        assertThat(foundUsers.get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should find users by keyword in name or email")
    void shouldFindUsersByKeyword() {
        // Given
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        // When
        List<User> foundByName = userRepository.findByKeyword("John");
        List<User> foundByEmail = userRepository.findByKeyword("smith");

        // Then
        assertThat(foundByName).hasSize(1);
        assertThat(foundByName.get(0).getName()).isEqualTo("John Doe");
        
        assertThat(foundByEmail).hasSize(1);
        assertThat(foundByEmail.get(0).getEmail()).isEqualTo("jane.smith@example.com");
    }

    @Test
    @DisplayName("Should check if user exists by email")
    void shouldCheckIfUserExistsByEmail() {
        // Given
        entityManager.persistAndFlush(testUser1);

        // When
        boolean exists = userRepository.existsByEmail("john.doe@example.com");
        boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
        // Given
        entityManager.persist(testUser1);
        entityManager.persist(testUser2);
        entityManager.flush();

        // When
        List<User> allUsers = userRepository.findAll();

        // Then
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith");
    }

    @Test
    @DisplayName("Should delete user by ID")
    void shouldDeleteUserById() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser1);
        Long userId = savedUser.getId();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should find user by provider and provider ID")
    void shouldFindUserByProviderAndProviderId() {
        // Given
        User oauthUser = new User("OAuth User", "oauth@example.com", AuthProvider.GOOGLE, "google123");
        entityManager.persistAndFlush(oauthUser);

        // When
        Optional<User> foundUser = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google123");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("OAuth User");
        assertThat(foundUser.get().getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(foundUser.get().getProviderId()).isEqualTo("google123");
    }

    @Test
    @DisplayName("Should return empty when user not found by provider and provider ID")
    void shouldReturnEmptyWhenUserNotFoundByProviderAndProviderId() {
        // When
        Optional<User> foundUser = userRepository.findByProviderAndProviderId(AuthProvider.KAKAO, "nonexistent123");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Should find users by provider")
    void shouldFindUsersByProvider() {
        // Given
        User googleUser1 = new User("Google User 1", "google1@example.com", AuthProvider.GOOGLE, "google123");
        User googleUser2 = new User("Google User 2", "google2@example.com", AuthProvider.GOOGLE, "google456");
        User kakaoUser = new User("Kakao User", "kakao@example.com", AuthProvider.KAKAO, "kakao123");
        
        entityManager.persist(googleUser1);
        entityManager.persist(googleUser2);
        entityManager.persist(kakaoUser);
        entityManager.flush();

        // When
        List<User> googleUsers = userRepository.findByProvider(AuthProvider.GOOGLE);

        // Then
        assertThat(googleUsers).hasSize(2);
        assertThat(googleUsers).extracting(User::getProvider)
                .containsOnly(AuthProvider.GOOGLE);
    }

    @Test
    @DisplayName("Should check if user exists by provider and provider ID")
    void shouldCheckIfUserExistsByProviderAndProviderId() {
        // Given
        User oauthUser = new User("OAuth User", "oauth@example.com", AuthProvider.APPLE, "apple123");
        entityManager.persistAndFlush(oauthUser);

        // When
        boolean exists = userRepository.existsByProviderAndProviderId(AuthProvider.APPLE, "apple123");
        boolean notExists = userRepository.existsByProviderAndProviderId(AuthProvider.APPLE, "nonexistent");

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should save user with OAuth provider information")
    void shouldSaveUserWithOAuthProviderInformation() {
        // Given
        User oauthUser = new User("OAuth User", "oauth@example.com", AuthProvider.KAKAO, "kakao123");
        oauthUser.setProfileImageUrl("https://example.com/profile.jpg");

        // When
        User savedUser = userRepository.save(oauthUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getProvider()).isEqualTo(AuthProvider.KAKAO);
        assertThat(savedUser.getProviderId()).isEqualTo("kakao123");
        assertThat(savedUser.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.isEnabled()).isTrue();
    }
}