package dev.designdeck.api.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "profiles")
public class Profile {
  @Id
  @Column(name = "user_id")
  private UUID userId;

  @MapsId
  @OneToOne(optional = false)
  @JoinColumn(name = "user_id")
  private AppUser user;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "daily_goal", nullable = false)
  private int dailyGoal = 10;

  @Column(name = "streak_count", nullable = false)
  private int streakCount = 0;

  @Column(name = "last_active_date")
  private LocalDate lastActiveDate;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Profile() {}

  public Profile(AppUser user, String displayName) {
    this.user = user;
    this.displayName = displayName;
  }

  @PrePersist
  void onCreate() {
    var now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getUserId() {
    return userId;
  }

  public AppUser getUser() {
    return user;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public int getDailyGoal() {
    return dailyGoal;
  }

  public void setDailyGoal(int dailyGoal) {
    this.dailyGoal = dailyGoal;
  }

  public int getStreakCount() {
    return streakCount;
  }

  public void setStreakCount(int streakCount) {
    this.streakCount = streakCount;
  }

  public LocalDate getLastActiveDate() {
    return lastActiveDate;
  }

  public void setLastActiveDate(LocalDate lastActiveDate) {
    this.lastActiveDate = lastActiveDate;
  }
}
