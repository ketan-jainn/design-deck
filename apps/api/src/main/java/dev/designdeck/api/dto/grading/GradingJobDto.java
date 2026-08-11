package dev.designdeck.api.dto.grading;

import dev.designdeck.api.entity.GradingJob;
import java.time.Instant;
import java.util.UUID;

public record GradingJobDto(
    UUID jobId,
    GradingJob.Status status,
    GradeDto result,
    Instant createdAt) {}
