package dev.designdeck.api.controller;

import dev.designdeck.api.dto.grading.GradeDto;
import dev.designdeck.api.dto.grading.GradeRequest;
import dev.designdeck.api.exception.ApiException;
import dev.designdeck.api.service.GradingService;
import dev.designdeck.api.service.RateLimiterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/grade")
public class GradeController {

  private static final int GRADE_LIMIT = 10;
  private static final Duration GRADE_WINDOW = Duration.ofHours(1);

  private final GradingService grading;
  private final RateLimiterService rateLimiter;

  public GradeController(GradingService grading, RateLimiterService rateLimiter) {
    this.grading = grading;
    this.rateLimiter = rateLimiter;
  }

  @PostMapping
  public GradeDto grade(@Valid @RequestBody GradeRequest req) {
    UUID userId = SecurityUtils.currentUserId();
    if (!rateLimiter.tryConsume(userId, "grade", GRADE_LIMIT, GRADE_WINDOW)) {
      throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
          "AI grading limit reached. You may submit up to " + GRADE_LIMIT + " gradings per hour.");
    }
    return grading.grade(req);
  }
}
