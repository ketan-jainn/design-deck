package dev.designdeck.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
  @Id
  private String token;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PasswordResetToken() {}

  public PasswordResetToken(String token, AppUser user, Instant expiresAt) {
    this.token = token;
    this.user = user;
    this.expiresAt = expiresAt;
  }

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
  }

  public String getToken() {
    return token;
  }

  public AppUser getUser() {
    return user;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getUsedAt() {
    return usedAt;
  }

  public void markUsed() {
    usedAt = Instant.now();
  }

  public boolean isValid() {
    return usedAt == null && expiresAt.isAfter(Instant.now());
  }
}
