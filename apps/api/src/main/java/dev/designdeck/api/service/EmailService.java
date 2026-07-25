package dev.designdeck.api.service;

public interface EmailService {
  void sendPasswordResetEmail(String to, String resetLink);
}
