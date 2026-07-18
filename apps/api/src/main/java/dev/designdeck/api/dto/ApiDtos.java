package dev.designdeck.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public final class ApiDtos {
  private ApiDtos() {}

  public record SignupRequest(@Email @NotBlank String email, @Size(min = 6, max = 200) String password, String displayName) {}
  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
  public record ForgotRequest(@Email @NotBlank String email) {}
  public record ResetRequest(@NotBlank String token, @Size(min = 6, max = 200) String password) {}
  public record UpdateProfileRequest(@NotBlank String displayName, @Min(1) @Max(100) int dailyGoal) {}
  public record SessionRequest(@Min(5) @Max(50) int size) {}
  public record AttemptRequest(UUID questionId, String selfRating, String userAnswer, Integer aiScore, Object aiFeedback) {}
  public record GradeRequest(UUID questionId, @NotBlank @Size(max = 4000) String userAnswer) {}
  public record AuthResponse(String accessToken, String refreshToken, ProfileDto profile) {}
  public record ProfileDto(String email, String displayName, int dailyGoal, int streak) {}
  public record CategoryDto(UUID id, String name, String slug, String color, int sortOrder) {}
  public record QuestionCategoryDto(String name, String slug, String color) {}
  public record AnswerKeyDto(List<String> bullets, String explanation, List<String> followUps, List<String> commonMistakes, String whenNotToUse) {}
  public record QuestionDto(UUID id, String prompt, String qtype, String difficulty, List<String> companies, List<String> sources, QuestionCategoryDto category, AnswerKeyDto answerKey) {}
  public record TopicMasteryDto(String name, String slug, String color, int mastery) {}
  public record ProgressSummary(int totalAnswered, int accuracy, int streak, int dailyGoal, int todayCount, int dueCount, List<TopicMasteryDto> weakest, List<TopicMasteryDto> strongest) {}
  public record GradeDto(int score, List<String> missing, List<String> wrong, List<String> improvements, String summary) {}
}