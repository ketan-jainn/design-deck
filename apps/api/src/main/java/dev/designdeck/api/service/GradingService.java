package dev.designdeck.api.service;

import dev.designdeck.api.dto.grading.GradeRequest;
import dev.designdeck.api.dto.grading.GradingJobDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.UUID;

public interface GradingService {
  GradingJobDto submitJob(UUID userId, GradeRequest req);
  SseEmitter streamJob(UUID userId, UUID jobId);
}
