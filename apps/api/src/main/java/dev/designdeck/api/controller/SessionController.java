package dev.designdeck.api.controller;

import dev.designdeck.api.dto.catalog.QuestionDto;
import dev.designdeck.api.dto.practice.SessionRequest;
import dev.designdeck.api.service.PracticeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
  private final PracticeService practice;

  public SessionController(PracticeService practice) {
    this.practice = practice;
  }

  @PostMapping
  public Map<String, List<QuestionDto>> session(@Valid @RequestBody SessionRequest req) {
    return Map.of("questions", practice.start(SecurityUtils.currentUserId(), req.size()));
  }
}
