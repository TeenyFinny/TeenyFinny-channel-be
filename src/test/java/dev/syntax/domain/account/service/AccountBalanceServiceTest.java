package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountSummaryRes;
import dev.syntax.domain.account.dto.AccountBalanceRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.card.repository.CardRepository;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

    @InjectMocks
    private AccountBalanceServiceImpl accountSummaryService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    /**
     * 테스트용 Mock UserContext 생성 헬퍼
     */
    private UserContext createMockUserContext(Long id, Role role, List<Long> children) {
        UserContext ctx = mock(UserContext.class);
        given(ctx.getId()).willReturn(id);
        lenient().when(ctx.getRole()).thenReturn(role.name());
        if (role == Role.PARENT) {
            lenient().when(ctx.getChildren()).thenReturn(children);
        }
        return ctx;
    }

    /**
     * 테스트용 Mock Account 생성 헬퍼
     */
    private Account createMockAccount(Long id, Long userId, AccountType type) {
        Account account = mock(Account.class);
        given(account.getId()).willReturn(id);
        // given(account.getAccountNo()).willReturn("123-456"); // 사용되지 않으므로 제거 (UnnecessaryStubbingException 방지)
        return account;
    }

    @Nested
    @DisplayName("✅ 계좌 요약 조회 성공 케이스")
    class SuccessTest {

    @Nested
    @DisplayName("🚫 권한 검증 실패 케이스")
    class FailTest {

        @Test
        @DisplayName("자녀가 다른 사람의 ID로 조회하면 예외가 발생한다.")
        void getSummary_Child_Other_Fail() {
            // given
            Long myId = 10L;
            Long otherId = 99L;
            UserContext ctx = createMockUserContext(myId, Role.CHILD, null);

            // when & then
            assertThatThrownBy(() -> accountSummaryService.getSummary(ctx, otherId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorBaseCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("부모가 내 자녀가 아닌 ID를 조회하면 예외가 발생한다.")
        void getSummary_Parent_NotMyChild_Fail() {
            // given
            Long parentId = 1L;
            Long myChildId = 10L;
            Long strangerChildId = 99L;
            
            // 내 자녀 목록에는 10번만 있음
            UserContext ctx = createMockUserContext(parentId, Role.PARENT, List.of(myChildId));

            // when & then
            assertThatThrownBy(() -> accountSummaryService.getSummary(ctx, strangerChildId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorBaseCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("권한 없는 사용자가 잔액 조회를 시도하면 예외가 발생한다.")
        void getBalance_Unauthorized_Fail() {
            // given
            Long myId = 10L;
            Long otherId = 99L;
            UserContext ctx = createMockUserContext(myId, Role.CHILD, null);

            // when & then
            assertThatThrownBy(() -> accountSummaryService.getBalance(ctx, otherId, AccountType.ALLOWANCE))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorBaseCode.UNAUTHORIZED);
        }
    }
    }
}