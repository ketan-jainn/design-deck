package dev.designdeck.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class FavoriteId implements Serializable {
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "question_id")
  private UUID questionId;

  protected FavoriteId() {}

  public FavoriteId(UUID userId, UUID questionId) {
    this.userId = userId;
    this.questionId = questionId;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getQuestionId() {
    return questionId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FavoriteId that)) return false;
    return Objects.equals(userId, that.userId) && Objects.equals(questionId, that.questionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, questionId);
  }
}
