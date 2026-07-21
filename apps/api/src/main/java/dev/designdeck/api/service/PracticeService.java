package dev.designdeck.api.service;

import dev.designdeck.api.dto.catalog.QuestionDto;
import dev.designdeck.api.dto.practice.AttemptRequest;
import dev.designdeck.api.dto.practice.ProgressSummary;
import java.util.List;
import java.util.UUID;

public interface PracticeService {
  List<QuestionDto> start(UUID userId, int size);

  void submit(UUID userId, AttemptRequest req);

  ProgressSummary summary(UUID userId);
}
