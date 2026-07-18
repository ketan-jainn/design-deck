package dev.designdeck.api.repository;

import dev.designdeck.api.dto.ApiDtos.ProfileDto;
import dev.designdeck.api.dto.ApiDtos.UpdateProfileRequest;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface AuthRepository {
  record UserCredentials(UUID userId, String passwordHash) {}

  void createUser(UUID userId, String email, String passwordHash);

  void createProfile(UUID userId, String displayName);

  Optional<UserCredentials> findCredentialsByEmail(String email);

  ProfileDto profile(UUID userId);

  ProfileDto updateProfile(UUID userId, UpdateProfileRequest req);

  Optional<UUID> findUserIdByEmail(String email);

  void createResetToken(String token, UUID userId, Instant expiresAt);

  Optional<UUID> findResetTokenUserId(String token);

  void updatePassword(UUID userId, String passwordHash);

  void markResetTokenUsed(String token);
}