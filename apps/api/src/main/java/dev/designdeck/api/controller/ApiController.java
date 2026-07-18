package dev.designdeck.api.controller;

import dev.designdeck.api.dto.ApiDtos.AttemptRequest;
import dev.designdeck.api.dto.ApiDtos.CategoryDto;
import dev.designdeck.api.dto.ApiDtos.GradeDto;
import dev.designdeck.api.dto.ApiDtos.GradeRequest;
import dev.designdeck.api.dto.ApiDtos.ProfileDto;
import dev.designdeck.api.dto.ApiDtos.ProgressSummary;
import dev.designdeck.api.dto.ApiDtos.QuestionDto;
import dev.designdeck.api.dto.ApiDtos.SessionRequest;
import dev.designdeck.api.dto.ApiDtos.UpdateProfileRequest;
import dev.designdeck.api.service.AuthService;
import dev.designdeck.api.service.CatalogService;
import dev.designdeck.api.service.GradingService;
import dev.designdeck.api.service.PracticeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
  private final CatalogService catalog;
  private final PracticeService practice;
  private final AuthService auth;
  private final GradingService grading;

  public ApiController(CatalogService catalog, PracticeService practice, AuthService auth, GradingService grading) {
    this.catalog = catalog;
    this.practice = practice;
    this.auth = auth;
    this.grading = grading;
  }

  @GetMapping("/me")
  ProfileDto me() {
    return auth.profile(currentUser());
  }

  @PatchMapping("/me")
  ProfileDto updateMe(@Valid @RequestBody UpdateProfileRequest req) {
    return auth.updateProfile(currentUser(), req);
  }

  @GetMapping("/categories")
  List<CategoryDto> categories() {
    return catalog.categories();
  }

  @GetMapping("/questions")
  List<QuestionDto> questions(@RequestParam(required = false) String topic, @RequestParam(required = false) String q) {
    return catalog.questions(topic, q);
  }

  @GetMapping("/questions/{id}")
  QuestionDto question(@PathVariable UUID id) {
    return catalog.question(id);
  }

  @PostMapping("/sessions")
  Map<String, List<QuestionDto>> session(@Valid @RequestBody SessionRequest req) {
    return Map.of("questions", practice.start(currentUser(), req.size()));
  }

  @PostMapping("/attempts")
  Map<String, Boolean> attempt(@Valid @RequestBody AttemptRequest req) {
    practice.submit(currentUser(), req);
    return Map.of("ok", true);
  }

  @GetMapping("/progress/summary")
  ProgressSummary summary() {
    return practice.summary(currentUser());
  }

  @PostMapping("/grade")
  GradeDto grade(@Valid @RequestBody GradeRequest req) {
    return grading.grade(req);
  }

  private UUID currentUser() {
    return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}