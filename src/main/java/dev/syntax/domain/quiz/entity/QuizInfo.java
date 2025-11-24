package dev.syntax.domain.quiz.entity;

import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 퀴즈 정보 엔티티
 */
@Entity
@Table(name = "quiz_info")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizInfo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long id;

    @Column(name = "title", columnDefinition = "TEXT", nullable = false)
    private String title;

    @Column(name = "info", columnDefinition = "TEXT", nullable = false)
    private String info;

    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(name = "answer", nullable = false)
    private String answer;

    @Column(name = "explanation", columnDefinition = "TEXT", nullable = false)
    private String explanation;
}
