package dev.designdeck.api.controller;

import dev.designdeck.api.dto.ApiDtos.AuthResponse;
import dev.designdeck.api.dto.ApiDtos.ForgotRequest;
import dev.designdeck.api.dto.ApiDtos.LoginRequest;
import dev.designdeck.api.dto.ApiDtos.ResetRequest;
import dev.designdeck.api.dto.ApiDtos.SignupRequest;
import dev.designdeck.api.security.JwtService;
import dev.designdeck.api.service.AuthService;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final JwtService jwt;
  private final AuthService auth;

  public AuthController(JwtService jwt, AuthService auth) {
    this.jwt = jwt;
    this.auth = auth;
  }

  @PostMapping("/signup")
  public AuthResponse signup(@Valid @RequestBody SignupRequest req) {
    return auth.signup(req, token -> jwt.issue(token, Duration.ofMinutes(15)), token -> jwt.issue(token, Duration.ofDays(30)));
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest req) {
    return auth.login(req, token -> jwt.issue(token, Duration.ofMinutes(15)), token -> jwt.issue(token, Duration.ofDays(30)));
  }

  @PostMapping("/logout")
  public Map<String, Boolean> logout() {
    return Map.of("ok", true);
  }

  @PostMapping("/forgot-password")
  public Map<String, Boolean> forgot(@Valid @RequestBody ForgotRequest req) {
    auth.createResetToken(req.email());
    return Map.of("ok", true);
  }

  @PostMapping("/reset-password")
  public Map<String, Boolean> reset(@Valid @RequestBody ResetRequest req) {
    auth.resetPassword(req.token(), req.password());
    return Map.of("ok", true);
  }
}