package dev.syntax.domain.quiz.repository;

import dev.syntax.domain.quiz.entity.QuizInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizInfoRepository extends JpaRepository<QuizInfo, Long> {
}
