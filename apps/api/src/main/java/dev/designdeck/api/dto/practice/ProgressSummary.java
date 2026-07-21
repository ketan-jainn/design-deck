package dev.designdeck.api.dto.practice;

import java.util.List;

public record ProgressSummary(
    int totalAnswered,
    int accuracy,
    int streak,
    int dailyGoal,
    int todayCount,
    int dueCount,
    List<TopicMasteryDto> weakest,
    List<TopicMasteryDto> strongest) {}
