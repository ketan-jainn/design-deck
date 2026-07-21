package dev.designdeck.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "answer_keys")
public class AnswerKey {
  @Id
  @Column(name = "question_id")
  private UUID questionId;

  @MapsId
  @OneToOne(optional = false)
  @JoinColumn(name = "question_id")
  private Question question;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private List<String> bullets = new ArrayList<>();

  @Column(nullable = false)
  private String explanation = "";

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "follow_ups", columnDefinition = "jsonb", nullable = false)
  private List<String> followUps = new ArrayList<>();

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "common_mistakes", columnDefinition = "jsonb", nullable = false)
  private List<String> commonMistakes = new ArrayList<>();

  @Column(name = "when_not_to_use", nullable = false)
  private String whenNotToUse = "";

  protected AnswerKey() {}

  public UUID getQuestionId() {
    return questionId;
  }

  public Question getQuestion() {
    return question;
  }

  public List<String> getBullets() {
    return bullets;
  }

  public String getExplanation() {
    return explanation;
  }

  public List<String> getFollowUps() {
    return followUps;
  }

  public List<String> getCommonMistakes() {
    return commonMistakes;
  }

  public String getWhenNotToUse() {
    return whenNotToUse;
  }
}
