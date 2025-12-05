package dev.syntax.domain.goal.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.goal.client.CoreGoalClient;
import dev.syntax.domain.goal.dto.CoreTransactionHistoryRes;
import dev.syntax.domain.goal.dto.GoalApproveRes;
import dev.syntax.domain.goal.dto.GoalCreateReq;
import dev.syntax.domain.goal.dto.GoalCreateRes;
import dev.syntax.domain.goal.dto.GoalDeleteRes;
import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.enums.GoalStatus;
import dev.syntax.domain.goal.repository.GoalRepository;
import dev.syntax.domain.notification.service.NotificationService;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.transfer.service.AutoTransferService;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    // Mock Objects
    @Mock private UserRepository userRepository;
    @Mock private GoalRepository goalRepository;
    @Mock private NotificationService notificationService;
    @Mock private CoreGoalClient coreGoalClient;
    @Mock private GoalAccountService goalAccountService;
    @Mock private AutoTransferService autoTransferService;
    @Mock private AccountRepository accountRepository;

    @InjectMocks
    private GoalServiceImpl goalService;

    // Test Data
    private User child;
    private User parent;
    private UserContext childContext;
    private UserContext parentContext;
    private Goal pendingGoal;
    private Goal ongoingGoal;
    private Account allowanceAccount;

    @BeforeEach
    void setUp() {
        // 1. User 엔티티 정의 (필수 필드 포함)
        child = User.builder()
                .id(1L).name("ChildName").email("child@dev").phoneNumber("010-1111-2222")
                .password("pw").simplePassword("1111").birthDate(LocalDate.now())
                .gender((byte) 1).role(Role.CHILD).coreUserId(100L)
                .parents(Collections.emptyList()).children(Collections.emptyList())
                .build();

        // 2. 자녀-부모 관계 (UserRelationship) Mocking
        // 부모 엔티티가 자녀 엔티티(child)를 포함하는 관계 객체를 생성합니다.
        UserRelationship childRelationship = UserRelationship.builder()
                .child(child)
                .parent(parent) // (양방향 설정을 위해 넣어줌)
                .build();
        List<UserRelationship> childrenList = List.of(childRelationship);

        // 3. Parent 엔티티 정의 (children 필드에 관계 목록 주입)
        parent = User.builder()
                .id(2L).name("ParentName").email("parent@dev").phoneNumber("010-3333-4444")
                .password("pw").simplePassword("1111").birthDate(LocalDate.now())
                .gender((byte) 1).role(Role.PARENT)
                .children(childrenList) // 👈 **이 부분이 핵심 수정**
                .build();

        // 4. UserContext 정의 (이제 parentContext는 자녀 ID 1L을 포함합니다)
        childContext = new UserContext(child);
        parentContext = new UserContext(parent);

        // 5. Goal 및 Account 정의
        ongoingGoal = Goal.builder()
                .id(11L).user(child).status(GoalStatus.ONGOING)
                .targetAmount(BigDecimal.valueOf(100000))
                .account(Account.builder().accountNo("2222-3333").build())
                .payDay(1).build();

        pendingGoal = Goal.builder()
                .id(10L).user(child).status(GoalStatus.PENDING)
                .name("Pending Goal")
                .targetAmount(BigDecimal.valueOf(100000)).build();

        allowanceAccount = Account.builder()
                .id(50L).type(AccountType.ALLOWANCE).accountNo("1111-2222").user(child).build();
    }

    // ----------------------------------------------------------------------------------
    // 1. TC-GOAL-001: 자녀가 목표 생성 (createGoal 성공)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-GOAL-001: 자녀가 목표 생성 성공")
    void createGoal_Success() {
        // GIVEN
        // GoalCreateReq를 Mock으로 생성하고 필요한 getter Stubbing (생성자 오류 회피)
        GoalCreateReq req = mock(GoalCreateReq.class);
        when(req.getTargetAmount()).thenReturn(BigDecimal.valueOf(50000));
        when(req.getMonthlyAmount()).thenReturn(BigDecimal.valueOf(10000));
        when(req.getPayDay()).thenReturn(5);
        when(req.getName()).thenReturn("Vacation Goal");

        // Mocking: 목표 중복 여부 확인
        when(goalRepository.existsByUserAndStatus(eq(child), eq(GoalStatus.PENDING))).thenReturn(false);
        when(goalRepository.existsByUserAndStatus(eq(child), eq(GoalStatus.ONGOING))).thenReturn(false);

        // Mocking: getParent() 호출
        when(userRepository.findById(any())).thenReturn(Optional.of(parent));

        // WHEN
        goalService.createGoal(childContext, req);

        // THEN
        verify(goalRepository, times(1)).save(any(Goal.class));
        verify(notificationService, times(1)).sendGoalRequestNotice(eq(parent), eq("ChildName"));
    }

    // ----------------------------------------------------------------------------------
    // 2. TC-GOAL-004: 부모가 목표 승인 (approveGoal 성공)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-GOAL-004: 부모가 목표 승인 성공 -> 계좌 생성 및 ONGOING 상태 확인")
    void approveGoal_ApproveSuccess() {
        // GIVEN
        Goal createdGoalWithAccount = Goal.builder()
                .id(10L).user(child).status(GoalStatus.ONGOING).build();

        when(goalRepository.findById(pendingGoal.getId())).thenReturn(Optional.of(pendingGoal));
        when(goalAccountService.createGoalAccount(any(Goal.class))).thenReturn(createdGoalWithAccount);

        // WHEN
        GoalApproveRes result = goalService.approveGoal(parentContext, pendingGoal.getId(), true);

        // THEN
        assertNotNull(result);
        assertEquals(GoalStatus.ONGOING, result.getStatus());
        verify(goalAccountService, times(1)).createGoalAccount(pendingGoal);
        verify(notificationService, times(1)).sendGoalAccountCreatedNotice(eq(child));
    }

    // ----------------------------------------------------------------------------------
    // 3. TC-GOAL-018: 부모가 목표 완료 확정 (confirmComplete 성공)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-GOAL-018: 부모가 목표 금액 달성 후 완료 확정 성공")
    void confirmComplete_GoalCompleted_Success() {
        // GIVEN
        when(goalRepository.findById(ongoingGoal.getId())).thenReturn(Optional.of(ongoingGoal));
        when(accountRepository.findByUserIdAndType(child.getId(), AccountType.ALLOWANCE)).thenReturn(Optional.of(allowanceAccount));

        // CoreTransactionHistoryRes를 Mock으로 생성하고 필요한 getter Stubbing (생성자 오류 회피)
        CoreTransactionHistoryRes completedHistory = mock(CoreTransactionHistoryRes.class);
        // validateGoalIsCompleted() 통과 조건: 잔액(100000)이 목표 금액(100000) 이상
        when(completedHistory.getBalance()).thenReturn(BigDecimal.valueOf(100000));
        when(coreGoalClient.getAccountHistory(anyString())).thenReturn(completedHistory);

        // WHEN
        goalService.confirmComplete(parentContext, ongoingGoal.getId());

        // THEN
        assertEquals(GoalStatus.COMPLETED, ongoingGoal.getStatus());
        verify(coreGoalClient, times(1)).updateAccountStatus(eq("2222-3333"), any());
        verify(autoTransferService, times(1)).deleteAutoTransfer(eq(allowanceAccount.getId()), eq(AutoTransferType.GOAL));
    }

    // ----------------------------------------------------------------------------------
    // 4. TC-GOAL-019: 목표 금액 미달성 상태에서 완료 요청 (requestComplete 실패)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-GOAL-019: 목표 금액 미달성 상태에서 자녀가 완료 요청 시 BusinessException 발생")
    void requestComplete_GoalNotCompleted_ThrowsException() {
        // GIVEN
        when(goalRepository.findById(ongoingGoal.getId())).thenReturn(Optional.of(ongoingGoal));

        // CoreTransactionHistoryRes를 Mock으로 생성하고 필요한 getter Stubbing (생성자 오류 회피)
        CoreTransactionHistoryRes incompleteHistory = mock(CoreTransactionHistoryRes.class);
        // validateGoalIsCompleted() 실패 조건: 잔액(50000)이 목표 금액(100000) 미만
        when(incompleteHistory.getBalance()).thenReturn(BigDecimal.valueOf(50000));
        when(coreGoalClient.getAccountHistory(anyString())).thenReturn(incompleteHistory);

        // WHEN & THEN
        assertThrows(BusinessException.class, () -> {
            goalService.requestComplete(childContext, ongoingGoal.getId());
        });

        // 실패 후 후속 작업이 없는지 검증
        verify(notificationService, never()).sendGoalCompleteRequestNotice(any(), any());
        verify(userRepository, never()).findById(any());
    }
}