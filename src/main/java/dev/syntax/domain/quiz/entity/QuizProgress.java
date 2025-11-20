package dev.syntax.domain.quiz.entity;

import dev.syntax.domain.user.entity.User;
import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "quiz_date")
    private LocalDate quizDate;

    @Builder.Default
    @Column(name = "streak_days")
    private Integer streakDays = 0;

    @Builder.Default
    @Column(name = "monthly_reward", nullable = false)
    private Boolean monthlyReward = false;

    @Builder.Default
    @Column(name = "course_completed")
    private Boolean courseCompleted = false;
}
