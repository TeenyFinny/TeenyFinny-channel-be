package dev.syntax.domain.quiz.service;

import dev.syntax.domain.quiz.dto.QuizInfoRes;
import dev.syntax.domain.quiz.dto.QuizProgressRes;
import dev.syntax.domain.quiz.entity.QuizInfo;
import dev.syntax.domain.quiz.entity.QuizProgress;
import dev.syntax.domain.quiz.repository.QuizInfoRepository;
import dev.syntax.domain.quiz.repository.QuizProgressRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;

import dev.syntax.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QuizServiceTest {

    private final QuizProgressRepository quizProgressRepositoryMock = Mockito.mock(QuizProgressRepository.class);
    private final QuizInfoRepository quizInfoRepositoryMock = Mockito.mock(QuizInfoRepository.class);
    private final QuizService service = new QuizServiceImpl(quizProgressRepositoryMock, quizInfoRepositoryMock);

    @Test
    @DisplayName("사용자 퀴즈 진행도 조회 성공")
    void getQuizProgress_success() {
        // 1) 테스트용 User 생성
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .role(Role.CHILD)
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
                .firstQuizIdToday(0)
                .build();

        Mockito.when( quizProgressRepositoryMock.findByUserId(1L)).thenReturn(Optional.of(progress));

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
        assertEquals(0, res.firstQuizIdToday());
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

        Mockito.when( quizProgressRepositoryMock.findByUserId(99L)).thenReturn(Optional.empty());

        try {
            service.getQuizProgress(context);
        } catch (BusinessException e) {
            assertEquals("대상을 찾을 수 없습니다.", e.getMessage());
        }
    }

    @Test
    @DisplayName("퀴즈 정보 조회 성공")
    void getQuizInfo_success() {
        // given
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .role(Role.CHILD)
                .children(Collections.emptyList())
                .parents(Collections.emptyList())
                .build();

        QuizInfo quizInfo = QuizInfo.builder()
                .id(1L)
                .title("주식이 뭐에요?")
                .info("주식은 분산 출자를 ...")
                .question("share는 주식을 세는 단위이다.")
                .answer("o")
                .explanation("share는 주식을 세는 단위가 맞습니다.")
                .build();

        Mockito.when( quizInfoRepositoryMock.findById(1L))
                .thenReturn(Optional.of(quizInfo));

        // when
        QuizInfoRes res = service.getQuizInfo(1L);

        // then
        assertEquals("주식이 뭐에요?", res.title());
        assertEquals("주식은 분산 출자를 ...", res.info());
        assertEquals("share는 주식을 세는 단위이다.", res.question());
        assertEquals("o", res.answer());
        assertEquals("share는 주식을 세는 단위가 맞습니다.", res.explanation());
    }

    @Test
    @DisplayName("퀴즈 정보 조회 실패 - 존재하지 않는 quizId")
    void getQuizInfo_notFound() {
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .role(Role.CHILD)
                .children(Collections.emptyList())
                .parents(Collections.emptyList())
                .build();

        Mockito.when( quizInfoRepositoryMock.findById(999L))
                .thenReturn(Optional.empty());

        try {
            service.getQuizInfo(999L);
        } catch (BusinessException e) {
            assertEquals("대상을 찾을 수 없습니다.", e.getMessage());
        }
    }

    @Test
    @DisplayName("사용자 퀴즈 진행도 생성 성공")
    void createQuizProgress_success() {
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .role(Role.CHILD)
                .children(Collections.emptyList())
                .parents(Collections.emptyList())
                .build();

        UserContext context = new UserContext(user);

        // progress가 존재하지 않는 경우
        Mockito.when(quizProgressRepositoryMock.findByUserId(1L))
                .thenReturn(Optional.empty());

        QuizProgress savedProgress = QuizProgress.builder()
                .id(12L)
                .userId(1L)
                .streakDays(0)
                .courseCompleted(false)
                .quizDate(0)
                .monthlyReward(false)
                .todaySolved(0)
                .coupon(0)
                .requestCompleted(false)
                .firstQuizIdToday(0)
                .build();

        Mockito.when(quizProgressRepositoryMock.save(Mockito.any(QuizProgress.class)))
                .thenReturn(savedProgress);

        QuizProgressRes res = service.createQuizProgress(context);

        assertEquals(12L, res.progressId());
        assertEquals(0, res.streakDays());
        assertEquals(false, res.courseCompleted());
        assertEquals(0, res.quizDate());
        assertEquals(false, res.monthlyReward());
        assertEquals(0, res.todaySolved());
        assertEquals(0, res.coupon());
        assertEquals(false, res.requestCompleted());
        assertEquals(0, res.firstQuizIdToday());
    }

    @Test
    @DisplayName("사용자 퀴즈 진행도 생성 실패 - 이미 존재하는 진행도")
    void createQuizProgress_alreadyExists() {
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .role(Role.CHILD)
                .children(Collections.emptyList())
                .parents(Collections.emptyList())
                .build();

        UserContext context = new UserContext(user);

        QuizProgress existingProgress = QuizProgress.builder()
                .id(12L)
                .userId(1L)
                .build();

        // 이미 progress가 존재하는 경우
        Mockito.when(quizProgressRepositoryMock.findByUserId(1L))
                .thenReturn(Optional.of(existingProgress));

        try {
            service.createQuizProgress(context);
        } catch (BusinessException e) {
            assertEquals("이미 존재하는 리소스입니다.", e.getMessage());
        }
    }
}
