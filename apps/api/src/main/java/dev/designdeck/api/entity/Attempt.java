package dev.designdeck.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "attempts")
public class Attempt {
  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Enumerated(EnumType.STRING)
  @Column(name = "self_rating")
  private SelfRating selfRating;

  @Column(name = "ai_score")
  private Integer aiScore;

  @Column(name = "user_answer")
  private String userAnswer;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "ai_feedback", columnDefinition = "jsonb")
  private String aiFeedback;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected Attempt() {}

  public Attempt(UUID id, AppUser user, Question question) {
    this.id = id;
    this.user = user;
    this.question = question;
  }

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public void setSelfRating(SelfRating selfRating) {
    this.selfRating = selfRating;
  }

  public void setAiScore(Integer aiScore) {
    this.aiScore = aiScore;
  }

  public void setUserAnswer(String userAnswer) {
    this.userAnswer = userAnswer;
  }

  public void setAiFeedback(String aiFeedback) {
    this.aiFeedback = aiFeedback;
  }
}
