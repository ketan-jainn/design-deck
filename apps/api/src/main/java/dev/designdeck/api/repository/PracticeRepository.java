package dev.designdeck.api.repository;

import dev.designdeck.api.dto.ApiDtos.AttemptRequest;
import dev.designdeck.api.dto.ApiDtos.TopicMasteryDto;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PracticeRepository {
  record CardState(double ease, int intervalDays, int timesSeen, int timesCorrect) {}

  record ProfileStats(int streakCount, int dailyGoal, LocalDate lastActiveDate) {}

  record Totals(int seen, int correct) {}

  List<UUID> dueQuestionIds(UUID userId, int limit);

  List<UUID> randomQuestionIds(UUID userId, int limit);

  void saveAttempt(UUID userId, AttemptRequest req, String aiFeedbackJson);

  CardState cardState(UUID userId, UUID questionId);

  void upsertCardState(UUID userId, UUID questionId, double ease, int intervalDays, Instant dueAt, int timesSeen, int timesCorrect, String lastResult);

  ProfileStats profileStats(UUID userId);

  Totals totals(UUID userId);

  Integer todayAttemptCount(UUID userId);

  Integer dueCount(UUID userId);

  List<TopicMasteryDto> topicMastery(UUID userId);

  void updateStreak(UUID userId, int streakCount, LocalDate lastActiveDate);
}