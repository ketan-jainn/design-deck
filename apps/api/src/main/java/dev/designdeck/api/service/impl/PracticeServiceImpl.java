package dev.designdeck.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.dto.catalog.QuestionDto;
import dev.designdeck.api.dto.practice.AttemptRequest;
import dev.designdeck.api.dto.practice.ProgressSummary;
import dev.designdeck.api.dto.practice.TopicMasteryDto;
import dev.designdeck.api.entity.AppUser;
import dev.designdeck.api.entity.Attempt;
import dev.designdeck.api.entity.Question;
import dev.designdeck.api.entity.SelfRating;
import dev.designdeck.api.entity.UserCardState;
import dev.designdeck.api.exception.ApiException;
import dev.designdeck.api.mapper.PracticeMapper;
import dev.designdeck.api.repository.AppUserRepository;
import dev.designdeck.api.repository.AttemptRepository;
import dev.designdeck.api.repository.ProfileRepository;
import dev.designdeck.api.repository.QuestionRepository;
import dev.designdeck.api.repository.UserCardStateRepository;
import dev.designdeck.api.service.CatalogService;
import dev.designdeck.api.service.PracticeService;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PracticeServiceImpl implements PracticeService {
  private final UserCardStateRepository userCardStateRepository;
  private final QuestionRepository questionRepository;
  private final AttemptRepository attemptRepository;
  private final ProfileRepository profileRepository;
  private final AppUserRepository appUserRepository;
  private final CatalogService catalogService;
  private final PracticeMapper practiceMapper;
  private final ObjectMapper mapper;

  public PracticeServiceImpl(
      UserCardStateRepository userCardStateRepository,
      QuestionRepository questionRepository,
      AttemptRepository attemptRepository,
      ProfileRepository profileRepository,
      AppUserRepository appUserRepository,
      CatalogService catalogService,
      PracticeMapper practiceMapper,
      ObjectMapper mapper) {
    this.userCardStateRepository = userCardStateRepository;
    this.questionRepository = questionRepository;
    this.attemptRepository = attemptRepository;
    this.profileRepository = profileRepository;
    this.appUserRepository = appUserRepository;
    this.catalogService = catalogService;
    this.practiceMapper = practiceMapper;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public List<QuestionDto> start(UUID userId, int size) {
    var dueIds = userCardStateRepository.findDueCards(userId, PageRequest.of(0, size)).stream()
        .map(state -> state.getId().getQuestionId())
        .toList();
    var ids = new ArrayList<>(dueIds);
    if (ids.size() < size) {
      ids.addAll(questionRepository.findRandomUnseenIds(userId, size - ids.size()));
    }
    return ids.stream().map(catalogService::question).toList();
  }

  @Override
  public void submit(UUID userId, AttemptRequest req) {
    var user = requireUser(userId);
    var question = requireQuestion(req.questionId());
    saveAttempt(user, question, req);
    updateCardState(user, question, req);
    updateStreak(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public ProgressSummary summary(UUID userId) {
    var profile = profileRepository
        .findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profile not found"));
    var totals = userCardStateRepository.sumTotals(userId);
    var seen = ((Number) totals[0]).intValue();
    var correct = ((Number) totals[1]).intValue();
    var today = (int) attemptRepository.countTodayAttempts(userId);
    var due = (int) userCardStateRepository.countDue(userId);
    var topics = userCardStateRepository.topicMastery(userId).stream()
        .map(practiceMapper::toTopicMasteryDto)
        .toList();
    var weakest = topics.stream().sorted(Comparator.comparing(TopicMasteryDto::mastery)).limit(3).toList();
    var strongest =
        topics.stream().sorted(Comparator.comparing(TopicMasteryDto::mastery).reversed()).limit(3).toList();
    return new ProgressSummary(
        seen,
        seen == 0 ? 0 : Math.round((float) correct * 100 / seen),
        profile.getStreakCount(),
        profile.getDailyGoal(),
        today,
        due,
        weakest,
        strongest);
  }

  private void saveAttempt(AppUser user, Question question, AttemptRequest req) {
    var attempt = new Attempt(UUID.randomUUID(), user, question);
    if (req.selfRating() != null) {
      attempt.setSelfRating(SelfRating.valueOf(req.selfRating()));
    }
    attempt.setAiScore(req.aiScore());
    attempt.setUserAnswer(req.userAnswer());
    attempt.setAiFeedback(writeJson(req.aiFeedback()));
    attemptRepository.save(attempt);
  }

  private void updateCardState(AppUser user, Question question, AttemptRequest req) {
    var state = userCardStateRepository.findByUser_IdAndQuestion_Id(user.getId(), question.getId()).orElse(null);
    var ladder = List.of(1, 3, 7, 14, 30);
    var ease = state == null ? 2.5d : state.getEase();
    var interval = state == null ? 0 : state.getIntervalDays();
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
    if (state == null) {
      state = new UserCardState(user, question);
    }
    state.setEase(ease);
    state.setIntervalDays(interval);
    state.setDueAt(due);
    state.setTimesSeen((state.getTimesSeen()) + 1);
    state.setTimesCorrect(state.getTimesCorrect() + (got ? 1 : 0));
    state.setLastResult(got ? "got" : "missed");
    userCardStateRepository.save(state);
  }

  private void updateStreak(UUID userId) {
    var profile = profileRepository
        .findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Profile not found"));
    var today = LocalDate.now(ZoneOffset.UTC);
    var last = profile.getLastActiveDate();
    if (last == null || !today.equals(last)) {
      var streak = last != null && today.minusDays(1).equals(last) ? profile.getStreakCount() + 1 : 1;
      profile.setStreakCount(streak);
      profile.setLastActiveDate(today);
      profileRepository.save(profile);
    }
  }

  private AppUser requireUser(UUID userId) {
    return appUserRepository
        .findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
  }

  private Question requireQuestion(UUID questionId) {
    return questionRepository
        .findById(questionId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
  }

  private String writeJson(Object value) {
    try {
      return value == null ? null : mapper.writeValueAsString(value);
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      return null;
    }
  }
}
