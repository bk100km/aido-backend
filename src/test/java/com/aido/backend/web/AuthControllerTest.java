package com.aido.backend.web;

import com.aido.backend.oauth.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return home page")
    void shouldReturnHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    @DisplayName("Should return login page")
    void shouldReturnLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @DisplayName("Should return login page with error message")
    void shouldReturnLoginPageWithErrorMessage() throws Exception {
        mockMvc.perform(get("/login")
                        .param("error", "true")
                        .param("message", "Authentication failed"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("error", true))
                .andExpect(model().attribute("errorMessage", "Authentication failed"));
    }

    @Test
    @DisplayName("Should return dashboard page for authenticated user")
    void shouldReturnDashboardPageForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/dashboard")
                        .with(oauth2Login()
                                .attributes(attrs -> {
                                    attrs.put("sub", "google123");
                                    attrs.put("name", "John Doe");
                                    attrs.put("email", "john@example.com");
                                })))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    @DisplayName("Should return dashboard page with success message")
    void shouldReturnDashboardPageWithSuccessMessage() throws Exception {
        mockMvc.perform(get("/dashboard")
                        .param("success", "true")
                        .with(oauth2Login()
                                .attributes(attrs -> {
                                    attrs.put("sub", "google123");
                                    attrs.put("name", "John Doe");
                                    attrs.put("email", "john@example.com");
                                })))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attribute("success", true))
                .andExpect(model().attribute("successMessage", "로그인에 성공했습니다!"));
    }

    @Test
    @DisplayName("Should return profile page for authenticated user")
    void shouldReturnProfilePageForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/profile")
                        .with(oauth2Login()
                                .attributes(attrs -> {
                                    attrs.put("sub", "google123");
                                    attrs.put("name", "John Doe");
                                    attrs.put("email", "john@example.com");
                                })))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"));
    }

    @Test
    @DisplayName("Should redirect to login for unauthenticated user accessing dashboard")
    void shouldRedirectToLoginForUnauthenticatedUserAccessingDashboard() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Should redirect to login for unauthenticated user accessing profile")
    void shouldRedirectToLoginForUnauthenticatedUserAccessingProfile() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection());
    }
}