package dev.designdeck.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ProblemDetail> handleApiException(ApiException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
    problem.setType(URI.create("https://api.designdeck.dev/errors/api-exception"));
    problem.setTitle(ex.getStatus().getReasonPhrase());
    return ResponseEntity.status(ex.getStatus()).body(problem);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed: " + message);
    problem.setType(URI.create("https://api.designdeck.dev/errors/validation"));
    problem.setTitle("Bad Request");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    problem.setType(URI.create("https://api.designdeck.dev/errors/internal"));
    problem.setTitle("Internal Server Error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }
}
