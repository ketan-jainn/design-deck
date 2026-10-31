package dev.designdeck.api.security;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ActuatorSecurityTest.ProbeController.class)
@Import({SecurityConfig.class, JwtFilter.class, ActuatorSecurityTest.ProbeController.class})
class ActuatorSecurityTest {
  @Autowired MockMvc mvc;
  @MockitoBean JwtService jwtService;

  @Test
  void anonymousUsersCanAccessOnlyLiveness() throws Exception {
    mvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
    mvc.perform(get("/actuator/health")).andExpect(status().isUnauthorized());
    mvc.perform(get("/actuator/health/readiness")).andExpect(status().isUnauthorized());
    mvc.perform(get("/actuator/prometheus")).andExpect(status().isUnauthorized());
  }

  @Test
  void ordinaryAuthenticatedUsersCannotAccessMetricsOrDetailedHealth() throws Exception {
    when(jwtService.verify("valid")).thenReturn(Optional.of(UUID.randomUUID()));
    mvc.perform(get("/actuator/health").header("Authorization", "Bearer valid"))
        .andExpect(status().isForbidden());
    mvc.perform(get("/actuator/metrics").header("Authorization", "Bearer valid"))
        .andExpect(status().isForbidden());
  }

  @RestController
  static class ProbeController {
    @GetMapping({"/actuator/health/liveness", "/actuator/health", "/actuator/health/readiness", "/actuator/metrics", "/actuator/prometheus"})
    String probe() {
      return "ok";
    }
  }
}
