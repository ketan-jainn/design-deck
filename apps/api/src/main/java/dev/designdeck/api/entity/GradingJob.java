package dev.designdeck.api.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "grading_jobs")
public class GradingJob {

  @Id
  private UUID id = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(columnDefinition = "TEXT")
  private String userAnswer;

  @Enumerated(EnumType.STRING)
  private Status status = Status.PENDING;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String result;

  private String errorMessage;
  private Instant createdAt = Instant.now();
  private Instant updatedAt = Instant.now();

  public enum Status {
    PENDING, PROCESSING, DONE, FAILED
  }

  public GradingJob() {}

  public GradingJob(AppUser user, Question question, String userAnswer) {
    this.user = user;
    this.question = question;
    this.userAnswer = userAnswer;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = Instant.now();
  }

  public UUID getId() { return id; }
  public AppUser getUser() { return user; }
  public Question getQuestion() { return question; }
  public String getUserAnswer() { return userAnswer; }
  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }
  public String getResult() { return result; }
  public void setResult(String result) { this.result = result; }
  public String getErrorMessage() { return errorMessage; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
