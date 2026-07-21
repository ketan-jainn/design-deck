package dev.designdeck.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_card_state")
public class UserCardState {
  @EmbeddedId
  private UserCardStateId id;

  @MapsId("userId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @MapsId("questionId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", nullable = false)
  private Question question;

  @Column(nullable = false)
  private double ease = 2.5;

  @Column(name = "interval_days", nullable = false)
  private int intervalDays = 0;

  @Column(name = "due_at", nullable = false)
  private Instant dueAt;

  @Column(name = "times_seen", nullable = false)
  private int timesSeen = 0;

  @Column(name = "times_correct", nullable = false)
  private int timesCorrect = 0;

  @Column(name = "last_result")
  private String lastResult;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected UserCardState() {}

  public UserCardState(AppUser user, Question question) {
    this.user = user;
    this.question = question;
    this.id = new UserCardStateId(user.getId(), question.getId());
    this.dueAt = Instant.now();
  }

  @PrePersist
  @PreUpdate
  void touch() {
    updatedAt = Instant.now();
  }

  public UserCardStateId getId() {
    return id;
  }

  public AppUser getUser() {
    return user;
  }

  public Question getQuestion() {
    return question;
  }

  public double getEase() {
    return ease;
  }

  public void setEase(double ease) {
    this.ease = ease;
  }

  public int getIntervalDays() {
    return intervalDays;
  }

  public void setIntervalDays(int intervalDays) {
    this.intervalDays = intervalDays;
  }

  public Instant getDueAt() {
    return dueAt;
  }

  public void setDueAt(Instant dueAt) {
    this.dueAt = dueAt;
  }

  public int getTimesSeen() {
    return timesSeen;
  }

  public void setTimesSeen(int timesSeen) {
    this.timesSeen = timesSeen;
  }

  public int getTimesCorrect() {
    return timesCorrect;
  }

  public void setTimesCorrect(int timesCorrect) {
    this.timesCorrect = timesCorrect;
  }

  public String getLastResult() {
    return lastResult;
  }

  public void setLastResult(String lastResult) {
    this.lastResult = lastResult;
  }
}
