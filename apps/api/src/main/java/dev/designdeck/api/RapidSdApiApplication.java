package dev.designdeck.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@SuppressWarnings("unused")
public class RapidSdApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(RapidSdApiApplication.class, args);
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
