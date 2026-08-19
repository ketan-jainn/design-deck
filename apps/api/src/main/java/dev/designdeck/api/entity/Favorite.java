package dev.designdeck.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "favorites")
public class Favorite {
  @EmbeddedId
  private FavoriteId id;

  @MapsId("userId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @MapsId("questionId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Favorite() {}

  public Favorite(AppUser user, Question question) {
    this.user = user;
    this.question = question;
    this.id = new FavoriteId(user.getId(), question.getId());
  }

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public FavoriteId getId() {
    return id;
  }

  public AppUser getUser() {
    return user;
  }

  public Question getQuestion() {
    return question;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
