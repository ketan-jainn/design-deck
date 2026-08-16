package dev.designdeck.api.controller;

import dev.designdeck.api.dto.grading.GradeRequest;
import dev.designdeck.api.dto.grading.GradingJobDto;
import dev.designdeck.api.exception.ApiException;
import dev.designdeck.api.service.GradingService;
import dev.designdeck.api.service.RateLimiterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/grade")
@Tag(name = "Grading", description = "Endpoints for AI grading of system design answers")
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
  @Operation(summary = "Submit an answer for grading", description = "Submits a user answer for AI grading. Returns a Job ID for async streaming. Rate limited.")
  public ResponseEntity<GradingJobDto> submitJob(@Valid @RequestBody GradeRequest req) {
    UUID userId = SecurityUtils.currentUserId();
    if (!rateLimiter.tryConsume(userId, "grade", GRADE_LIMIT, GRADE_WINDOW)) {
      throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
          "AI grading limit reached. You may submit up to " + GRADE_LIMIT + " gradings per hour.");
    }
    return ResponseEntity.accepted().body(grading.submitJob(userId, req));
  }

  @GetMapping("/{jobId}/stream")
  @Operation(summary = "Stream grading results", description = "Streams the result of a grading job via Server-Sent Events (SSE).")
  public SseEmitter streamJob(@PathVariable UUID jobId) {
    return grading.streamJob(SecurityUtils.currentUserId(), jobId);
  }
}
