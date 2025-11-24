package dev.syntax.domain.quiz.dto;

import lombok.Builder;

@Builder
public record QuizInfoRes(
        String title,
        String info,
        String question,
        String answer,
        String explanation
) {}
