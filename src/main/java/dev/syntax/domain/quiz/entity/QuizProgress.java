package dev.syntax.domain.quiz.entity;

import jakarta.persistence.*;

import lombok.*;

/**
 * 퀴즈 진행도 엔티티
 */
@Entity
@Table(name = "quiz_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private int streakDays;
    private boolean courseCompleted;
    private int quizDate;
    private boolean monthlyReward;
    private int todaySolved;
    private int coupon;
    private boolean requestCompleted;
}
