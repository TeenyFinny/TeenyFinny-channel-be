package dev.syntax.domain.quiz.entity;

import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import lombok.*;

/**
 * 퀴즈 진행도 엔티티
 */
@Entity
@Table(name = "quiz_progress")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Builder.Default
    @Column(name = "streak_days", nullable = false)
    private int streakDays = 0;

    @Builder.Default
    @Column(name = "course_completed", nullable = false)
    private boolean courseCompleted = false;

    @Column(name = "quiz_date", nullable = false)
    private int quizDate;

    @Builder.Default
    @Column(name = "monthly_reward", nullable = false)
    private boolean monthlyReward = false;

    @Builder.Default
    @Column(name = "today_solved", nullable = false)
    private int todaySolved = 0;

    @Builder.Default
    @Column(name = "coupon", nullable = false)
    private int coupon = 0;

    @Builder.Default
    @Column(name = "request_completed", nullable = false)
    private boolean requestCompleted = false;
}
