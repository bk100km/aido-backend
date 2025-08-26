package com.aido.backend.controller;

import com.aido.backend.dto.UserDto;
import com.aido.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserDto = new UserDto(1L, "John Doe", "john.doe@example.com");
    }

    @Test
    @DisplayName("Should get all users")
    void shouldGetAllUsers() throws Exception {
        // Given
        UserDto userDto2 = new UserDto(2L, "Jane Smith", "jane.smith@example.com");
        List<UserDto> users = Arrays.asList(testUserDto, userDto2);
        
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[0].email", is("john.doe@example.com")))
                .andExpect(jsonPath("$[1].name", is("Jane Smith")))
                .andExpect(jsonPath("$[1].email", is("jane.smith@example.com")));

        verify(userService).getAllUsers();
    }

    @Test
    @DisplayName("Should get user by ID when user exists")
    void shouldGetUserByIdWhenUserExists() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUserDto));

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("Should return 404 when user not found by ID")
    void shouldReturn404WhenUserNotFoundById() throws Exception {
        // Given
        when(userService.getUserById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    @DisplayName("Should get user by email when user exists")
    void shouldGetUserByEmailWhenUserExists() throws Exception {
        // Given
        when(userService.getUserByEmail("john.doe@example.com")).thenReturn(Optional.of(testUserDto));

        // When & Then
        mockMvc.perform(get("/api/users/email/john.doe@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));

        verify(userService).getUserByEmail("john.doe@example.com");
    }

    @Test
    @DisplayName("Should search users by keyword")
    void shouldSearchUsersByKeyword() throws Exception {
        // Given
        List<UserDto> users = Arrays.asList(testUserDto);
        when(userService.searchUsers("John")).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users/search").param("keyword", "John"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")));

        verify(userService).searchUsers("John");
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() throws Exception {
        // Given
        UserDto newUserDto = new UserDto(null, "John Doe", "john.doe@example.com");
        UserDto createdUserDto = new UserDto(1L, "John Doe", "john.doe@example.com");
        
        when(userService.createUser(any(UserDto.class))).thenReturn(createdUserDto);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    @DisplayName("Should return 400 when creating user with invalid data")
    void shouldReturn400WhenCreatingUserWithInvalidData() throws Exception {
        // Given
        UserDto invalidUserDto = new UserDto(null, "", "invalid-email");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserDto.class));
    }

    @Test
    @DisplayName("Should return 400 when creating user with existing email")
    void shouldReturn400WhenCreatingUserWithExistingEmail() throws Exception {
        // Given
        UserDto newUserDto = new UserDto(null, "John Doe", "john.doe@example.com");
        
        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isBadRequest());

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        UserDto updateUserDto = new UserDto(1L, "John Updated", "john.updated@example.com");
        
        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(updateUserDto);

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("John Updated")))
                .andExpect(jsonPath("$.email", is("john.updated@example.com")));

        verify(userService).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    @DisplayName("Should return 400 when updating user with invalid data")
    void shouldReturn400WhenUpdatingUserWithInvalidData() throws Exception {
        // Given
        UserDto invalidUserDto = new UserDto(1L, "", "invalid-email");

        // When & Then
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(anyLong(), any(UserDto.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() throws Exception {
        // Given
        doNothing().when(userService).deleteUser(1L);

        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existing user")
    void shouldReturn404WhenDeletingNonExistingUser() throws Exception {
        // Given
        doThrow(new IllegalArgumentException("User not found"))
                .when(userService).deleteUser(999L);

        // When & Then
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(999L);
    }
}