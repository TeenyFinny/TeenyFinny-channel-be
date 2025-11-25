package dev.syntax.domain.goal;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreGoalAccountRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.goal.dto.GoalAccountCreateRes;
import dev.syntax.domain.goal.enums.GoalStatus;
import dev.syntax.domain.goal.repository.GoalRepository;
import dev.syntax.domain.goal.service.GoalAccountServiceImpl;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalAccountServiceImplTest {

    @Mock
    private CoreAccountClient coreAccountClient;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private GoalRepository goalRepository;

    @InjectMocks
    private GoalAccountServiceImpl goalAccountService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).build();
    }

    @Test
    @DisplayName("목표 계좌 생성 성공")
    void createGoalAccount_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.existsByUserAndStatus(user, GoalStatus.ONGOING)).thenReturn(false);
        when(coreAccountClient.createGoalAccount(1L, "저축목표"))
                .thenReturn(new CoreGoalAccountRes("123-456-7890",1L,new BigDecimal(0)));

        GoalAccountCreateRes res = goalAccountService.createGoalAccount(1L, "저축목표");

        assertTrue(res.isSuccess());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("목표 계좌 생성 실패 - 사용자 없음")
    void createGoalAccount_fail_userNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        GoalAccountCreateRes res = goalAccountService.createGoalAccount(99L, "저축목표");
        assertFalse(res.isSuccess());
    }

    @Test
    @DisplayName("목표 계좌 생성 실패 - 이미 진행 중인 목표 존재")
    void createGoalAccount_fail_goalAlreadyOngoing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.existsByUserAndStatus(user, GoalStatus.ONGOING)).thenReturn(true);

        GoalAccountCreateRes res = goalAccountService.createGoalAccount(1L, "저축목표");
        assertFalse(res.isSuccess());
    }

    @Test
    @DisplayName("목표 계좌 생성 실패 - 코어 응답 null")
    void createGoalAccount_fail_coreResNull() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(goalRepository.existsByUserAndStatus(user, GoalStatus.ONGOING)).thenReturn(false);
        when(coreAccountClient.createGoalAccount(1L, "저축목표")).thenReturn(null);

        GoalAccountCreateRes res = goalAccountService.createGoalAccount(1L, "저축목표");
        assertFalse(res.isSuccess());
    }
}
