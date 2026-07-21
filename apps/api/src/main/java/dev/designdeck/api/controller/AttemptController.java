package dev.designdeck.api.controller;

import dev.designdeck.api.dto.practice.AttemptRequest;
import dev.designdeck.api.service.PracticeService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {
  private final PracticeService practice;

  public AttemptController(PracticeService practice) {
    this.practice = practice;
  }

  @PostMapping
  public Map<String, Boolean> attempt(@Valid @RequestBody AttemptRequest req) {
    practice.submit(SecurityUtils.currentUserId(), req);
    return Map.of("ok", true);
  }
}
