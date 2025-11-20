package dev.syntax.domain.feedback.repository;

import dev.syntax.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
