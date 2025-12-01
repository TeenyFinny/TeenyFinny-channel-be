package dev.syntax.domain.quiz.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.syntax.domain.quiz.entity.QuizProgress;

public interface QuizProgressRepository extends JpaRepository<QuizProgress, Long> {

    Optional<QuizProgress> findByUserId(Long userId);
}
