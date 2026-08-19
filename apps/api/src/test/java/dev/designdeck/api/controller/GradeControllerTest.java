package dev.designdeck.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.dto.grading.GradeRequest;
import dev.designdeck.api.dto.grading.GradingJobDto;
import dev.designdeck.api.entity.GradingJob;
import dev.designdeck.api.security.JwtService;
import dev.designdeck.api.service.GradingService;
import dev.designdeck.api.service.RateLimiterService;
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

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GradeController.class)
@AutoConfigureMockMvc(addFilters = false)
class GradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GradingService gradingService;

    @MockBean
    private RateLimiterService rateLimiterService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserRepository appUserRepository;

    private MockedStatic<SecurityUtils> mockedSecurityUtils;
    private final UUID testUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockedSecurityUtils = Mockito.mockStatic(SecurityUtils.class);
        mockedSecurityUtils.when(SecurityUtils::currentUserId).thenReturn(testUserId);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtils.close();
    }

    @Test
    void submitGrade_rateLimitExceeded_returnsTooManyRequests() throws Exception {
        Mockito.when(rateLimiterService.tryConsume(any(), anyString(), anyInt(), any())).thenReturn(false);

        mockMvc.perform(post("/api/grade")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void submitGrade_validRequest_returnsAccepted() throws Exception {
        Mockito.when(rateLimiterService.tryConsume(any(), anyString(), anyInt(), any())).thenReturn(true);
        Mockito.when(gradingService.submitJob(any(), any())).thenReturn(
            new GradingJobDto(UUID.randomUUID(), GradingJob.Status.PENDING, null, Instant.now()));

        GradeRequest request = new GradeRequest(UUID.randomUUID(), "some answer");

        mockMvc.perform(post("/api/grade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }
}
