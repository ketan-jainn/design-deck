package dev.designdeck.api.service;

import dev.designdeck.api.dto.auth.AuthResponse;
import dev.designdeck.api.dto.auth.LoginRequest;
import dev.designdeck.api.dto.auth.SignupRequest;
import dev.designdeck.api.dto.profile.ProfileDto;
import dev.designdeck.api.dto.profile.UpdateProfileRequest;
import java.util.UUID;
import java.util.function.Function;

public interface AuthService {
  AuthResponse signup(SignupRequest req, Function<UUID, String> accessTokenIssuer, Function<UUID, String> refreshTokenIssuer);

  AuthResponse login(LoginRequest req, Function<UUID, String> accessTokenIssuer, Function<UUID, String> refreshTokenIssuer);

  ProfileDto profile(UUID userId);

  ProfileDto updateProfile(UUID userId, UpdateProfileRequest req);

  void createResetToken(String email);

  void resetPassword(String token, String password);
}
