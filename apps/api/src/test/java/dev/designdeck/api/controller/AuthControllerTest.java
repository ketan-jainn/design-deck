package dev.designdeck.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.dto.auth.AuthResponse;
import dev.designdeck.api.dto.auth.SignupRequest;
import dev.designdeck.api.security.JwtService;
import dev.designdeck.api.service.AuthService;
import dev.designdeck.api.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserRepository appUserRepository;

    @Test
    void signup_validRequest_returnsOk() throws Exception {
        SignupRequest request = new SignupRequest("test@example.com", "Password123!", "Test User");

        AuthResponse authResponse = new AuthResponse("access", "refresh");
        Mockito.when(authService.signup(any(), any(), any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void signup_invalidEmail_returnsBadRequest() throws Exception {
        SignupRequest request = new SignupRequest("invalid-email", "Password123!", "Test User");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
