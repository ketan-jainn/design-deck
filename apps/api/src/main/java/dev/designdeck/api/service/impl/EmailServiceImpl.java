package dev.designdeck.api.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import dev.designdeck.api.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {
  private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

  private final ObjectProvider<JavaMailSender> mailSenderProvider;
  private final String fromAddress;
  private final String smtpHost;

  public EmailServiceImpl(
      ObjectProvider<JavaMailSender> mailSenderProvider,
      @Value("${designdeck.mail.from:}") String fromAddress,
      @Value("${spring.mail.host:}") String smtpHost) {
    this.mailSenderProvider = mailSenderProvider;
    this.fromAddress = fromAddress;
    this.smtpHost = smtpHost;
  }

  @Override
  public void sendPasswordResetEmail(String to, String resetLink) {
    if (!isMailConfigured()) {
      log.info("Password reset link for {}: {}", to, resetLink);
      return;
    }

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(to);
    message.setSubject("Reset your RapidSD password");
    message.setText("""
        You requested a password reset for your RapidSD account.

        Reset your password using this link:
        %s

        This link expires in 1 hour. If you did not request a reset, you can ignore this email.
        """.formatted(resetLink));

    mailSenderProvider.getObject().send(message);
    log.info("Sent password reset email to {}", to);
  }

  private boolean isMailConfigured() {
    return StringUtils.hasText(smtpHost)
        && StringUtils.hasText(fromAddress)
        && mailSenderProvider.getIfAvailable() != null;
  }
}
