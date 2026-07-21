package dev.designdeck.api.controller;

import dev.designdeck.api.dto.practice.ProgressSummary;
import dev.designdeck.api.service.PracticeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {
  private final PracticeService practice;

  public ProgressController(PracticeService practice) {
    this.practice = practice;
  }

  @GetMapping("/summary")
  public ProgressSummary summary() {
    return practice.summary(SecurityUtils.currentUserId());
  }
}
