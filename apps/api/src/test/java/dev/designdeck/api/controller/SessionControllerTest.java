package dev.designdeck.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.security.JwtService;
import dev.designdeck.api.service.PracticeService;
import dev.designdeck.api.repository.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PracticeService practiceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserRepository appUserRepository;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::currentUserId).thenReturn(UUID.randomUUID());
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    void createSession_invalidSize_returnsBadRequest() throws Exception {
        String requestJson = "{\"size\":50}";

        mockMvc.perform(post("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }
}
