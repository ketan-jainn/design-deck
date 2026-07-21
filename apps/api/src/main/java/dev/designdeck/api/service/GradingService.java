package dev.designdeck.api.service;

import dev.designdeck.api.dto.grading.GradeDto;
import dev.designdeck.api.dto.grading.GradeRequest;

public interface GradingService {
  GradeDto grade(GradeRequest req);
}
