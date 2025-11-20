package dev.syntax.domain.quiz.repository;

import dev.syntax.domain.quiz.entity.QuizProgress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizProgressRepository extends JpaRepository<QuizProgress, Long> {
}
