package dev.designdeck.api.repository.impl;

import dev.designdeck.api.dto.ApiDtos.ProfileDto;
import dev.designdeck.api.dto.ApiDtos.UpdateProfileRequest;
import dev.designdeck.api.repository.AuthRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepositoryImpl implements AuthRepository {
  private final JdbcClient jdbc;

  public AuthRepositoryImpl(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void createUser(UUID userId, String email, String passwordHash) {
    jdbc.sql("insert into app_users (id, email, password_hash) values (:id, :email, :hash)")
        .param("id", userId)
        .param("email", email)
        .param("hash", passwordHash)
        .update();
  }

  @Override
  public void createProfile(UUID userId, String displayName) {
    jdbc.sql("insert into profiles (user_id, display_name) values (:id, :name)")
        .param("id", userId)
        .param("name", displayName)
        .update();
  }

  @Override
  public java.util.Optional<UserCredentials> findCredentialsByEmail(String email) {
    return jdbc.sql("select id, password_hash from app_users where email = :email")
        .param("email", email)
        .query((rs, n) -> new UserCredentials(rs.getObject("id", UUID.class), rs.getString("password_hash")))
        .optional();
  }

  @Override
  public ProfileDto profile(UUID userId) {
    return jdbc.sql("""
        select u.email, p.display_name, p.daily_goal, p.streak_count
        from app_users u join profiles p on p.user_id = u.id where u.id = :id
        """)
        .param("id", userId)
        .query((rs, n) -> new ProfileDto(rs.getString("email"), rs.getString("display_name"), rs.getInt("daily_goal"), rs.getInt("streak_count")))
        .single();
  }

  @Override
  public ProfileDto updateProfile(UUID userId, UpdateProfileRequest req) {
    jdbc.sql("update profiles set display_name = :name, daily_goal = :goal, updated_at = now() where user_id = :id")
        .param("name", req.displayName())
        .param("goal", req.dailyGoal())
        .param("id", userId)
        .update();
    return profile(userId);
  }

  @Override
  public java.util.Optional<UUID> findUserIdByEmail(String email) {
    return jdbc.sql("select id from app_users where email = :email")
        .param("email", email)
        .query(UUID.class)
        .optional();
  }

  @Override
  public void createResetToken(String token, UUID userId, Instant expiresAt) {
    jdbc.sql("insert into password_reset_tokens (token, user_id, expires_at) values (:token, :userId, :expires)")
        .param("token", token)
        .param("userId", userId)
        .param("expires", expiresAt)
        .update();
  }

  @Override
  public java.util.Optional<UUID> findResetTokenUserId(String token) {
    return jdbc.sql("select user_id from password_reset_tokens where token = :token and used_at is null and expires_at > now()")
        .param("token", token)
        .query(UUID.class)
        .optional();
  }

  @Override
  public void updatePassword(UUID userId, String passwordHash) {
    jdbc.sql("update app_users set password_hash = :hash where id = :id")
        .param("hash", passwordHash)
        .param("id", userId)
        .update();
  }

  @Override
  public void markResetTokenUsed(String token) {
    jdbc.sql("update password_reset_tokens set used_at = now() where token = :token")
        .param("token", token)
        .update();
  }
}