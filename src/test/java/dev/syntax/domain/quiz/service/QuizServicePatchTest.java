//package dev.syntax.domain.quiz.service;
//
//import dev.syntax.domain.quiz.dto.QuizProgressRes;
//import dev.syntax.domain.quiz.dto.QuizProgressUpdateReq;
//import dev.syntax.domain.quiz.entity.QuizProgress;
//import dev.syntax.domain.quiz.repository.QuizProgressRepository;
//import dev.syntax.global.auth.dto.UserContext;
//import dev.syntax.domain.user.entity.User;
//import dev.syntax.domain.user.enums.Role;
//import dev.syntax.global.exception.BusinessException;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//
//import java.util.Collections;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//class QuizServicePatchTest {
//
//    private final QuizProgressRepository progressRepo = Mockito.mock(QuizProgressRepository.class);
//    private final QuizService service = new QuizServiceImpl(progressRepo, null); // QuizInfoRepository 필요 없음
//
//    @Test
//    @DisplayName("퀴즈 진행도 PATCH - 오늘 푼 문제 수 업데이트 성공")
//    void updateQuizProgress_success() {
//        // given
//        User user = User.builder()
//                .id(1L)
//                .email("test@test.com")
//                .role(Role.CHILD)
//                .children(Collections.emptyList())
//                .parents(Collections.emptyList())
//                .build();
//        UserContext context = new UserContext(user);
//
//        QuizProgress existingProgress = QuizProgress.builder()
//                .id(1L)
//                .userId(1L)
//                .streakDays(3)
//                .courseCompleted(true)
//                .quizDate(15)
//                .monthlyReward(false)
//                .todaySolved(1)
//                .coupon(0)
//                .requestCompleted(false)
//                .firstQuizIdToday(0)
//                .build();
//
//        Mockito.when(progressRepo.findByUserId(1L)).thenReturn(Optional.of(existingProgress));
//        Mockito.when(progressRepo.save(Mockito.any(QuizProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//        // when
//        QuizProgressUpdateReq req = new QuizProgressUpdateReq();
//        req.setTodaySolved(2);
//
//        QuizProgressRes updatedRes = service.updateQuizProgress(context, req);
//
//        // then
//        assertEquals(1L, updatedRes.progressId());
//        assertEquals(2, updatedRes.todaySolved());
//    }
//
//    @Test
//    @DisplayName("퀴즈 진행도 PATCH 실패 - 존재하지 않는 유저")
//    void updateQuizProgress_notFound() {
//        User user = User.builder()
//                .id(99L)
//                .email("notfound@test.com")
//                .role(Role.CHILD)
//                .children(Collections.emptyList())
//                .parents(Collections.emptyList())
//                .build();
//        UserContext context = new UserContext(user);
//
//        Mockito.when(progressRepo.findByUserId(99L)).thenReturn(Optional.empty());
//
//        try {
//            QuizProgressUpdateReq req = new QuizProgressUpdateReq();
//            req.setTodaySolved(2);
//
//            service.updateQuizProgress(context, req);
//        } catch (BusinessException e) {
//            assertEquals("대상을 찾을 수 없습니다.", e.getMessage());
//        }
//    }
//}
