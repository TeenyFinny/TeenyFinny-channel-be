package dev.syntax.domain.quiz.service;

import dev.syntax.domain.notification.service.NotificationService;
import dev.syntax.domain.quiz.dto.QuizProgressRes;
import dev.syntax.domain.quiz.dto.QuizProgressUpdateReq;
import dev.syntax.domain.quiz.dto.RequestCompletedRes;
import dev.syntax.domain.quiz.entity.QuizProgress;
import dev.syntax.domain.quiz.repository.QuizProgressRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    // Mock Repositories and Services
    @Mock private QuizProgressRepository quizProgressRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    // QuizInfoRepository는 요청된 TC에 사용되지 않으므로 제외하거나 Mock만 선언

    @InjectMocks
    private QuizServiceImpl quizService;

    // Test Data
    private User child;
    private User parent;
    private UserContext childContext;
    private QuizProgress quizProgress;
    private final Long PARENT_ID = 2L;

    @BeforeEach
    void setUp() {
        // 1. Child User & Context Setup
        child = User.builder()
                .id(1L).name("ChildName").role(Role.CHILD)
                .build();

        // 2. Parent User 초기 생성 (자녀-부모 관계 설정을 위해 필수)
        parent = User.builder()
                .id(PARENT_ID).name("ParentName").role(Role.PARENT)
                .build();

        // 3. Mock Parent-Child Relationship 생성
        // UserRelationship은 child와 parent 인스턴스를 모두 필요로 합니다.
        UserRelationship childRelationship = UserRelationship.builder()
                .child(child)
                .parent(parent)
                .build();
        List<UserRelationship> childrenList = List.of(childRelationship);

        // 4. Parent User 최종 재빌드 (children List를 포함하여 완성)
        // GoalService에서 성공적으로 사용된 부모-자녀 연관관계 설정 메커니즘을 반영했습니다.
        parent = User.builder()
                .id(PARENT_ID)
                .name("ParentName")
                .role(Role.PARENT)
                .children(childrenList)
                .build();

        // 5. UserContext Setup
        childContext = new UserContext(child);

        // 6. QuizProgress Entity Setup
        quizProgress = QuizProgress.builder()
                .id(1L)
                .userId(child.getId())
                .streakDays(0)
                .courseCompleted(false)
                .quizDate(LocalDate.now().getDayOfMonth())
                .monthlyReward(false)
                .todaySolved(0)
                .coupon(0)
                .requestCompleted(true)
                .firstQuizIdToday(0)
                .build();
    }

    /**
     * QuizProgress 엔티티에 toBuilder() 메서드가 없으므로,
     * 복사를 수행하는 헬퍼 메서드를 정의하여 toBuilder() 사용을 대체합니다.
     */
    private QuizProgress.QuizProgressBuilder copyQuizProgress(QuizProgress original) {
        return QuizProgress.builder()
                .id(original.getId())
                .userId(original.getUserId())
                .streakDays(original.getStreakDays())
                .courseCompleted(original.isCourseCompleted())
                .quizDate(original.getQuizDate())
                .monthlyReward(original.isMonthlyReward())
                .todaySolved(original.getTodaySolved())
                .coupon(original.getCoupon())
                .requestCompleted(original.isRequestCompleted())
                .firstQuizIdToday(original.getFirstQuizIdToday());
    }

    // ----------------------------------------------------------------------------------
    // 1. TC-QUIZ-002: 퀴즈 진행도 생성 (createQuizProgress 성공)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-QUIZ-002: 사용자의 퀴즈 진행도 생성 성공")
    void createQuizProgress_Success() {
        // GIVEN
        when(quizProgressRepository.findByUserId(child.getId())).thenReturn(Optional.empty());
        // QuizProgress의 toBuilder() 대신 헬퍼 메서드 사용
        QuizProgress savedProgress = copyQuizProgress(quizProgress).id(200L).build();
        when(quizProgressRepository.save(any(QuizProgress.class))).thenReturn(savedProgress);

        ArgumentCaptor<QuizProgress> progressCaptor = ArgumentCaptor.forClass(QuizProgress.class);

        // WHEN
        QuizProgressRes result = quizService.createQuizProgress(childContext);

        // THEN
        assertNotNull(result);
        assertEquals(200L, result.progressId());
        assertEquals(0, result.streakDays());

        // quizProgressRepository.save()가 호출되었는지 검증하고, 저장된 객체의 초기값 확인
        verify(quizProgressRepository, times(1)).save(progressCaptor.capture());
        QuizProgress capturedProgress = progressCaptor.getValue();
        assertEquals(0, capturedProgress.getStreakDays());
        assertFalse(capturedProgress.isCourseCompleted());
        assertEquals(0, capturedProgress.getTodaySolved());
    }

    // ----------------------------------------------------------------------------------
    // 2. TC-QUIZ-003: 퀴즈 진행도 업데이트 (updateQuizProgress 성공)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-QUIZ-003: 퀴즈 진행도 업데이트 성공 - todaySolved 및 coupon 업데이트")
    void updateQuizProgress_Success() {
        // GIVEN
        int newTodaySolved = 6;
        int newCoupon = 2;

        // 1. 업데이트 요청 DTO Mocking (⚠️ builder() 오류 회피 및 특정 필드만 설정)
        QuizProgressUpdateReq req = mock(QuizProgressUpdateReq.class);
        // 업데이트할 필드에 값 설정
        when(req.getCoupon()).thenReturn(newCoupon);
        when(req.getTodaySolved()).thenReturn(newTodaySolved);

        // 업데이트되지 않는 나머지 필드는 null을 반환하도록 설정하여 QuizProgress.update() 로직을 충족
        when(req.getQuizDate()).thenReturn(null);
        when(req.getCourseCompleted()).thenReturn(null);
        when(req.getMonthlyReward()).thenReturn(null);
        when(req.getRequestCompleted()).thenReturn(null);
        when(req.getFirstQuizIdToday()).thenReturn(null);

        // 2. 기존 진행도 조회 Mocking
        when(quizProgressRepository.findByUserId(child.getId())).thenReturn(Optional.of(quizProgress));

        // 3. save 호출 시 업데이트된 객체 반환 Mocking (QuizProgress의 toBuilder() 대신 헬퍼 메서드 사용)
        QuizProgress updatedProgress = copyQuizProgress(quizProgress)
                .todaySolved(newTodaySolved)
                .coupon(newCoupon)
                .build();

        when(quizProgressRepository.save(any(QuizProgress.class))).thenReturn(updatedProgress);

        // WHEN
        QuizProgressRes result = quizService.updateQuizProgress(childContext, req);

        // THEN
        assertNotNull(result);
        assertEquals(newTodaySolved, result.todaySolved());
        assertEquals(newCoupon, result.coupon());
        assertEquals(quizProgress.getStreakDays(), result.streakDays());

        verify(quizProgressRepository, times(1)).save(any(QuizProgress.class));
    }

    // ----------------------------------------------------------------------------------
    // 3. TC-QUIZ-006: 투자 계좌 요청 알림 전송 (sendInvestmentAccountRequest 성공)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-QUIZ-006: 자녀가 부모에게 투자 계좌 요청 알림 전송 성공")
    void sendInvestmentAccountRequest_Success() {
        // GIVEN
        // getParent() 로직 Mocking: childContext의 PARENT_ID로 부모 User를 조회하면 설정된 parent 객체를 반환
        when(userRepository.findById(childContext.getParentId())).thenReturn(Optional.of(parent));

        // WHEN
        quizService.sendInvestmentAccountRequest(childContext);

        // THEN
        // 1. getParent() 호출 검증
        verify(userRepository, times(1)).findById(eq(childContext.getParentId()));
        // 2. 알림 전송 서비스 호출 검증
        verify(notificationService, times(1)).sendInvestmentAccountRequestNotice(
                eq(parent),
                eq(child.getName())
        );
    }

    // ----------------------------------------------------------------------------------
    // 4. TC-QUIZ-008: 부모가 requestCompleted 상태 업데이트 (updateRequestCompleted 성공)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-QUIZ-008: 부모가 자녀의 퀴즈 진행도에서 requestCompleted 상태를 false로 업데이트 성공")
    void updateRequestCompleted_Success() {
        // GIVEN
        when(quizProgressRepository.findByUserId(child.getId())).thenReturn(Optional.of(quizProgress));

        // 2. save 호출 시 requestCompleted가 false로 업데이트된 객체 반환 Mocking (QuizProgress의 toBuilder() 대신 헬퍼 메서드 사용)
        QuizProgress updatedProgress = copyQuizProgress(quizProgress)
                .requestCompleted(false)
                .build();

        when(quizProgressRepository.save(any(QuizProgress.class))).thenReturn(updatedProgress);

        // WHEN
        // 부모의 UserContext는 실제 로직에서 자녀-부모 관계 검증 시 사용되나, 현재 서비스 로직에는 미반영.
        UserContext parentContext = new UserContext(parent);
        RequestCompletedRes result = quizService.updateRequestCompleted(parentContext, child.getId());

        // THEN
        assertNotNull(result);
        assertFalse(result.requestCompleted());

        verify(quizProgressRepository, times(1)).save(any(QuizProgress.class));
    }
}