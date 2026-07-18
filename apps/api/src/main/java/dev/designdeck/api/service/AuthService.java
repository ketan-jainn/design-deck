package dev.designdeck.api.service;

import dev.designdeck.api.dto.ApiDtos.ProfileDto;
import dev.designdeck.api.dto.ApiDtos.SignupRequest;
import dev.designdeck.api.dto.ApiDtos.LoginRequest;
import dev.designdeck.api.dto.ApiDtos.UpdateProfileRequest;
import dev.designdeck.api.dto.ApiDtos.AuthResponse;
import dev.designdeck.api.exception.ApiException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import dev.designdeck.api.repository.AuthRepository;

@Service
public class AuthService {
  private final AuthRepository repository;
  private final PasswordEncoder encoder;
  private final String frontendUrl;

  public AuthService(AuthRepository repository, PasswordEncoder encoder, @Value("${designdeck.frontend-url}") String frontendUrl) {
    this.repository = repository;
    this.encoder = encoder;
    this.frontendUrl = frontendUrl;
  }

  public AuthResponse signup(SignupRequest req, Function<UUID, String> accessTokenIssuer, Function<UUID, String> refreshTokenIssuer) {
    var userId = UUID.randomUUID();
    repository.createUser(userId, req.email().toLowerCase(), encoder.encode(req.password()));
    repository.createProfile(userId, req.displayName() == null || req.displayName().isBlank() ? req.email().split("@")[0] : req.displayName());
    return new AuthResponse(accessTokenIssuer.apply(userId), refreshTokenIssuer.apply(userId), profile(userId));
  }

  public AuthResponse login(LoginRequest req, Function<UUID, String> accessTokenIssuer, Function<UUID, String> refreshTokenIssuer) {
    var row = repository.findCredentialsByEmail(req.email().toLowerCase())
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
    if (!encoder.matches(req.password(), row.passwordHash())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
    return new AuthResponse(accessTokenIssuer.apply(row.userId()), refreshTokenIssuer.apply(row.userId()), profile(row.userId()));
  }

  public ProfileDto profile(UUID userId) {
    return repository.profile(userId);
  }

  public ProfileDto updateProfile(UUID userId, UpdateProfileRequest req) {
    return repository.updateProfile(userId, req);
  }

  public void createResetToken(String email) {
    repository.findUserIdByEmail(email.toLowerCase()).ifPresent(userId -> {
      var token = UUID.randomUUID().toString() + UUID.randomUUID();
      repository.createResetToken(token, userId, Instant.now().plus(Duration.ofHours(1)));
      System.out.println("Password reset link: " + frontendUrl + "/reset-password?token=" + token);
    });
  }

  public void resetPassword(String token, String password) {
    var userId = repository.findResetTokenUserId(token)
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Reset token is invalid or expired"));
    repository.updatePassword(userId, encoder.encode(password));
    repository.markResetTokenUsed(token);
  }
}