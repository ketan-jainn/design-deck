package dev.designdeck.api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<String> api(ApiException e) {
    return ResponseEntity.status(e.status).body(e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<String> generic(Exception e) {
    return ResponseEntity.status(500).body(e.getMessage());
  }
}
