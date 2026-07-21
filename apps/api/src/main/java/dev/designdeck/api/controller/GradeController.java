package dev.designdeck.api.controller;

import dev.designdeck.api.dto.grading.GradeDto;
import dev.designdeck.api.dto.grading.GradeRequest;
import dev.designdeck.api.service.GradingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grade")
public class GradeController {
  private final GradingService grading;

  public GradeController(GradingService grading) {
    this.grading = grading;
  }

  @PostMapping
  public GradeDto grade(@Valid @RequestBody GradeRequest req) {
    return grading.grade(req);
  }
}
