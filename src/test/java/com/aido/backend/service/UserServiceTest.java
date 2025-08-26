package com.aido.backend.service;

import com.aido.backend.dto.UserDto;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = new User("John Doe", "john.doe@example.com");
        testUser.setId(1L);
        
        testUserDto = new UserDto(1L, "John Doe", "john.doe@example.com");
    }

    @Test
    @DisplayName("Should get all users")
    void shouldGetAllUsers() {
        // Given
        User user2 = new User("Jane Smith", "jane.smith@example.com");
        user2.setId(2L);
        List<User> users = Arrays.asList(testUser, user2);
        
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(1).getName()).isEqualTo("Jane Smith");
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should get user by ID when user exists")
    void shouldGetUserByIdWhenUserExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<UserDto> result = userService.getUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    void shouldReturnEmptyWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        Optional<UserDto> result = userService.getUserById(999L);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get user by email when user exists")
    void shouldGetUserByEmailWhenUserExists() {
        // Given
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<UserDto> result = userService.getUserByEmail("john.doe@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("John Doe");
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should search users by keyword")
    void shouldSearchUsersByKeyword() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findByKeyword("John")).thenReturn(users);

        // When
        List<UserDto> result = userService.searchUsers("John");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        verify(userRepository).findByKeyword("John");
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        // Given
        UserDto newUserDto = new UserDto(null, "John Doe", "john.doe@example.com");
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDto result = userService.createUser(newUserDto);

        // Then
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with existing email")
    void shouldThrowExceptionWhenCreatingUserWithExistingEmail() {
        // Given
        UserDto newUserDto = new UserDto(null, "John Doe", "john.doe@example.com");
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(newUserDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
        
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        // Given
        UserDto updateUserDto = new UserDto(1L, "John Updated", "john.updated@example.com");
        User existingUser = new User("John Doe", "john.doe@example.com");
        existingUser.setId(1L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("john.updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // When
        UserDto result = userService.updateUser(1L, updateUserDto);

        // Then
        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john.updated@example.com");
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("john.updated@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing user")
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {
        // Given
        UserDto updateUserDto = new UserDto(999L, "John Updated", "john.updated@example.com");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(999L, updateUserDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with id: 999");
        
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when updating user with existing email")
    void shouldThrowExceptionWhenUpdatingUserWithExistingEmail() {
        // Given
        UserDto updateUserDto = new UserDto(1L, "John Updated", "existing@example.com");
        User existingUser = new User("John Doe", "john.doe@example.com");
        existingUser.setId(1L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, updateUserDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
        
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing user")
    void shouldThrowExceptionWhenDeletingNonExistingUser() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found with id: 999");
        
        verify(userRepository).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should handle OAuth user creation with provider information")
    void shouldHandleOAuthUserCreationWithProviderInformation() {
        // Given
        UserDto oauthUserDto = new UserDto(null, "OAuth User", "oauth@example.com");
        User oauthUser = new User("OAuth User", "oauth@example.com", AuthProvider.GOOGLE, "google123");
        oauthUser.setId(1L);
        
        when(userRepository.existsByEmail("oauth@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(oauthUser);

        // When
        UserDto result = userService.createUser(oauthUserDto);

        // Then
        assertThat(result.getName()).isEqualTo("OAuth User");
        assertThat(result.getEmail()).isEqualTo("oauth@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle user with default LOCAL provider")
    void shouldHandleUserWithDefaultLocalProvider() {
        // Given
        User localUser = new User("Local User", "local@example.com");
        localUser.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(localUser));

        // When
        Optional<UserDto> result = userService.getUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("local@example.com");
        assertThat(localUser.getProvider()).isEqualTo(AuthProvider.LOCAL); // Default provider
    }
}