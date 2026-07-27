package dev.designdeck.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class EmailServiceImpl implements EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
  private static final String RESEND_URL = "https://api.resend.com/emails";

  private final String resendApiKey;
  private final String fromAddress;
  private final ObjectMapper mapper;
  private final HttpClient httpClient;

  public EmailServiceImpl(
      @Value("${designdeck.resend-api-key:}") String resendApiKey,
      @Value("${designdeck.mail.from:noreply@rapidsd.dev}") String fromAddress,
      ObjectMapper mapper) {
    this.resendApiKey = resendApiKey;
    this.fromAddress = fromAddress;
    this.mapper = mapper;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();
  }

  @Override
  public void sendPasswordResetEmail(String to, String resetLink) {
    if (!isResendConfigured()) {
      log.info("[DEV] Password reset link for {}: {}", to, resetLink);
      return;
    }
    try {
      Map<String, Object> payload = Map.of(
          "from", fromAddress,
          "to", List.of(to),
          "subject", "Reset your RapidSD password",
          "text", buildResetEmailBody(resetLink)
      );
      String body = mapper.writeValueAsString(payload);
      HttpRequest request = HttpRequest.newBuilder(URI.create(RESEND_URL))
          .timeout(Duration.ofSeconds(10))
          .header("Authorization", "Bearer " + resendApiKey)
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .build();
      HttpResponse<String> response = httpClient
          .sendAsync(request, HttpResponse.BodyHandlers.ofString())
          .join();
      if (response.statusCode() >= 400) {
        log.error("Resend API error {} for {}: {}", response.statusCode(), to, response.body());
      } else {
        log.info("Password reset email sent to {} via Resend", to);
      }
    } catch (Exception e) {
      log.error("Failed to send password reset email to {}: {}", to, e.getMessage());
      // Do not rethrow — email failure must not break the reset-token flow
    }
  }

  private boolean isResendConfigured() {
    return StringUtils.hasText(resendApiKey);
  }

  private String buildResetEmailBody(String resetLink) {
    return """
        You requested a password reset for your RapidSD account.

        Reset your password using this link:
        %s

        This link expires in 1 hour. If you did not request a reset, you can safely ignore this email.
        """.formatted(resetLink);
  }
}
