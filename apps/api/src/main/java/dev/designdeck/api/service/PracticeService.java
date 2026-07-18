package dev.designdeck.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.dto.ApiDtos.AttemptRequest;
import dev.designdeck.api.dto.ApiDtos.ProgressSummary;
import dev.designdeck.api.dto.ApiDtos.QuestionDto;
import dev.designdeck.api.dto.ApiDtos.TopicMasteryDto;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import dev.designdeck.api.repository.PracticeRepository;
import org.springframework.stereotype.Service;

@Service
public class PracticeService {
  private final PracticeRepository repository;
  private final CatalogService catalog;
  private final ObjectMapper mapper;

  public PracticeService(PracticeRepository repository, CatalogService catalog, ObjectMapper mapper) {
    this.repository = repository;
    this.catalog = catalog;
    this.mapper = mapper;
  }

  public List<QuestionDto> start(UUID userId, int size) {
    var dueIds = repository.dueQuestionIds(userId, size);
    var ids = new ArrayList<>(dueIds);
    if (ids.size() < size) {
      ids.addAll(repository.randomQuestionIds(userId, size - ids.size()));
    }
    return ids.stream().map(catalog::question).toList();
  }

  public void submit(UUID userId, AttemptRequest req) {
    repository.saveAttempt(userId, req, writeJson(req.aiFeedback()));
    var state = repository.cardState(userId, req.questionId());
    var ladder = List.of(1, 3, 7, 14, 30);
    var ease = state == null ? 2.5d : state.ease();
    var interval = state == null ? 0 : state.intervalDays();
    var got = "got".equals(req.selfRating()) || (req.aiScore() != null && req.aiScore() >= 70);
    if (got) {
      ease = Math.min(3.0d, ease + 0.1d);
      var nextInterval = 30;
      for (var day : ladder) {
        if (day > interval) {
          nextInterval = day;
          break;
        }
      }
      interval = nextInterval;
    } else {
      ease = Math.max(1.3d, ease - 0.2d);
      interval = 0;
    }
    var due = Instant.now().plus(Duration.ofDays(interval));
    repository.upsertCardState(userId, req.questionId(), ease, interval, due, (state == null ? 0 : state.timesSeen()) + 1, (state == null ? 0 : state.timesCorrect()) + (got ? 1 : 0), got ? "got" : "missed");
    updateStreak(userId);
  }

  public ProgressSummary summary(UUID userId) {
    var profile = repository.profileStats(userId);
    var totals = repository.totals(userId);
    var seen = totals.seen();
    var correct = totals.correct();
    var today = repository.todayAttemptCount(userId);
    var due = repository.dueCount(userId);
    var topics = repository.topicMastery(userId);
    var weakest = topics.stream().sorted(java.util.Comparator.comparing(TopicMasteryDto::mastery)).limit(3).toList();
    var strongest = topics.stream().sorted(java.util.Comparator.comparing(TopicMasteryDto::mastery).reversed()).limit(3).toList();
    return new ProgressSummary(seen, seen == 0 ? 0 : Math.round((float) correct * 100 / seen), profile.streakCount(), profile.dailyGoal(), today == null ? 0 : today, due == null ? 0 : due, weakest, strongest);
  }

  private void updateStreak(UUID userId) {
    var row = repository.profileStats(userId);
    var today = LocalDate.now(ZoneOffset.UTC);
    var last = row.lastActiveDate();
    if (last == null || !today.equals(last)) {
      var streak = last != null && today.minusDays(1).equals(last) ? row.streakCount() + 1 : 1;
      repository.updateStreak(userId, streak, today);
    }
  }

  private String writeJson(Object value) {
    try {
      return value == null ? null : mapper.writeValueAsString(value);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      return null;
    }
  }
}