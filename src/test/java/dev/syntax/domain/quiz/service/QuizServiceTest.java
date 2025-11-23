package dev.syntax.domain.quiz.service;

import dev.syntax.domain.quiz.dto.QuizProgressRes;
import dev.syntax.domain.quiz.entity.QuizProgress;
import dev.syntax.domain.quiz.repository.QuizProgressRepository;
import dev.syntax.domain.quiz.service.impl.QuizServiceImpl;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuizServiceTest {

    private final QuizProgressRepository repository = Mockito.mock(QuizProgressRepository.class);
    private final QuizService service = new QuizServiceImpl(repository);

    @Test
    @DisplayName("사용자 퀴즈 진행도 조회 성공")
    void getQuizProgress_success() {
        // 1) 테스트용 User 생성
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .role(Role.PARENT)
                .children(Collections.emptyList())
                .parents(Collections.emptyList())
                .build();

        UserContext context = new UserContext(user);

        // 2) 테스트용 QuizProgress
        QuizProgress progress = QuizProgress.builder()
                .id(2L)
                .userId(1L)
                .streakDays(3)
                .courseCompleted(true)
                .quizDate(15)
                .monthlyReward(false)
                .todaySolved(1)
                .coupon(0)
                .requestCompleted(false)
                .build();

        Mockito.when(repository.findByUserId(1L)).thenReturn(Optional.of(progress));

        // 3) Service 호출
        QuizProgressRes res = service.getQuizProgress(context);

        // 4) 검증
        assertEquals(2L, res.progressId());
        assertEquals(3, res.streakDays());
        assertEquals(true, res.courseCompleted());
        assertEquals(15, res.quizDate());
        assertEquals(false, res.monthlyReward());
        assertEquals(1, res.todaySolved());
        assertEquals(0, res.coupon());
        assertEquals(false, res.requestCompleted());
    }

    @Test
    @DisplayName("사용자 퀴즈 진행도 조회 실패 - 존재하지 않는 유저")
    void getQuizProgress_notFound() {
        User user = User.builder()
                .id(99L)
                .email("notfound@test.com")
                .role(Role.PARENT)
                .children(Collections.emptyList())
                .parents(Collections.emptyList())
                .build();

        UserContext context = new UserContext(user);

        Mockito.when(repository.findByUserId(99L)).thenReturn(Optional.empty());

        try {
            service.getQuizProgress(context);
        } catch (IllegalArgumentException e) {
            assertEquals("사용자 진행도가 존재하지 않습니다.", e.getMessage());
        }
    }
}
