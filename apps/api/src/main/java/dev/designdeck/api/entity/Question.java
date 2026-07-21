package dev.designdeck.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "questions")
public class Question {
  @Id
  private UUID id;

  @Column(nullable = false)
  private String prompt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QuestionType qtype;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Difficulty difficulty = Difficulty.sde2;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(columnDefinition = "text[]", nullable = false)
  private String[] companies = new String[0];

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(columnDefinition = "text[]", nullable = false)
  private String[] sources = new String[0];

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @OneToOne(mappedBy = "question", fetch = FetchType.LAZY)
  private AnswerKey answerKey;

  protected Question() {}

  public UUID getId() {
    return id;
  }

  public String getPrompt() {
    return prompt;
  }

  public QuestionType getQtype() {
    return qtype;
  }

  public Difficulty getDifficulty() {
    return difficulty;
  }

  public Category getCategory() {
    return category;
  }

  public String[] getCompanies() {
    return companies;
  }

  public String[] getSources() {
    return sources;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public AnswerKey getAnswerKey() {
    return answerKey;
  }

  @PrePersist
  void onCreate() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
