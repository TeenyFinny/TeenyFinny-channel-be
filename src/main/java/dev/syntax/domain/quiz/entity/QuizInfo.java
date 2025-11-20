package dev.syntax.domain.quiz.entity;

import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_info")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long id;

    // 퀴즈 정보
    @Column(name = "info", nullable = false, columnDefinition = "TEXT")
    private String info;

    // 질문
    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    // 정답
    @Column(name = "answer", nullable = false, length = 100)
    private String answer;

    // 해설
    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    // 교육과정 포함 여부
    @Builder.Default
    @Column(name = "course", nullable = false)
    private Boolean course = true;
}
