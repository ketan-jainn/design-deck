package dev.designdeck.api.repository;

import dev.designdeck.api.entity.AppUser;
import dev.designdeck.api.entity.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
  @Query("""
      select t from PasswordResetToken t
      where t.token = :token and t.usedAt is null and t.expiresAt > CURRENT_TIMESTAMP
      """)
  Optional<PasswordResetToken> findValidToken(@Param("token") String token);

  @Modifying
  @Query("""
      update PasswordResetToken t
      set t.usedAt = CURRENT_TIMESTAMP
      where t.user = :user and t.usedAt is null
      """)
  void invalidateActiveTokensForUser(@Param("user") AppUser user);
}
