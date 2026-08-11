package dev.designdeck.api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.designdeck.api.dto.grading.GradeDto;
import dev.designdeck.api.dto.grading.GradeRequest;
import dev.designdeck.api.dto.grading.GradingJobDto;
import dev.designdeck.api.entity.AppUser;
import dev.designdeck.api.entity.GradingJob;
import dev.designdeck.api.entity.Question;
import dev.designdeck.api.exception.ApiException;
import dev.designdeck.api.repository.AppUserRepository;
import dev.designdeck.api.repository.GradingJobRepository;
import dev.designdeck.api.repository.QuestionRepository;
import dev.designdeck.api.service.GradingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

@Service
public class GradingServiceImpl implements GradingService {
  private final GradingJobRepository gradingJobRepository;
  private final AppUserRepository appUserRepository;
  private final QuestionRepository questionRepository;
  private final AsyncGradingWorker asyncWorker;
  private final SseEmitterRegistry sseEmitterRegistry;
  private final ObjectMapper mapper;

  public GradingServiceImpl(
      GradingJobRepository gradingJobRepository,
      AppUserRepository appUserRepository,
      QuestionRepository questionRepository,
      AsyncGradingWorker asyncWorker,
      SseEmitterRegistry sseEmitterRegistry,
      ObjectMapper mapper) {
    this.gradingJobRepository = gradingJobRepository;
    this.appUserRepository = appUserRepository;
    this.questionRepository = questionRepository;
    this.asyncWorker = asyncWorker;
    this.sseEmitterRegistry = sseEmitterRegistry;
    this.mapper = mapper;
  }

  @Override
  public GradingJobDto submitJob(UUID userId, GradeRequest req) {
    AppUser user = appUserRepository.findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    Question question = questionRepository.findById(req.questionId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Question not found"));
        
    GradingJob job = new GradingJob(user, question, req.userAnswer());
    gradingJobRepository.save(job);
    
    asyncWorker.processJob(job.getId(), question.getId(), req.userAnswer());
    
    return new GradingJobDto(job.getId(), job.getStatus(), null, job.getCreatedAt());
  }

  @Override
  public SseEmitter streamJob(UUID userId, UUID jobId) {
    GradingJob job = gradingJobRepository.findByIdAndUser_Id(jobId, userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Job not found"));
        
    if (job.getStatus() == GradingJob.Status.DONE) {
      SseEmitter emitter = new SseEmitter();
      try {
        GradeDto result = mapper.readValue(job.getResult(), GradeDto.class);
        emitter.send(SseEmitter.event().name("complete").data(result));
        emitter.complete();
      } catch (Exception e) {
        emitter.completeWithError(e);
      }
      return emitter;
    }
    
    if (job.getStatus() == GradingJob.Status.FAILED) {
      SseEmitter emitter = new SseEmitter();
      try {
        emitter.send(SseEmitter.event().name("error").data(Map.of("message", job.getErrorMessage())));
        emitter.completeWithError(new RuntimeException(job.getErrorMessage()));
      } catch (Exception e) {
        emitter.completeWithError(e);
      }
      return emitter;
    }
    
    SseEmitter emitter = new SseEmitter(35000L); // 35 seconds
    sseEmitterRegistry.register(jobId, emitter);
    return emitter;
  }
}
