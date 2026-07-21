package dev.designdeck.api.repository;

import dev.designdeck.api.entity.AnswerKey;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerKeyRepository extends JpaRepository<AnswerKey, UUID> {}
