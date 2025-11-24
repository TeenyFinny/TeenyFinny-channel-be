package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountHistoryReq;
import dev.syntax.domain.account.dto.AccountHistoryRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountHistoryServiceTest {

    @InjectMocks
    private AccountHistoryServiceImpl service;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserContext userContext;

    /**
     * ===============================
     *  1. 거래내역 정상 조회 테스트
     * ===============================
     */
    @Test
    @DisplayName("거래내역 조회 - 정상적으로 월별 거래내역이 반환된다")
    void getHistory_success() {
        // given
        Long userId = 10L;
        AccountHistoryReq req = new AccountHistoryReq(AccountType.ALLOWANCE, 2025, 1);

        Account account = Account.builder()
                .accountNo("123-456")
                .build();

        // 권한: 자녀가 본인 계좌 조회
        given(userContext.getId()).willReturn(userId);
        given(userContext.getRole()).willReturn(Role.CHILD.name());

        // 계좌 존재
        given(accountRepository.findByUserIdAndType(eq(userId), eq(AccountType.ALLOWANCE)))
                .willReturn(Optional.of(account));

        // mockCoreHistory() 스파이로 감싸서 실제 mock 데이터 기반 테스트
        AccountHistoryServiceImpl spyService = Mockito.spy(service);

        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 31, 23, 59, 59);

        List<AccountHistoryRes> mockData = List.of(
                new AccountHistoryRes("T1", "deposit", "이체", "50,000", "150,000", "2025-01-10 12:00"),
                new AccountHistoryRes("T2", "withdrawal", "편의점", "1,500", "148,500", "2025-01-15 14:00")
        );

        Mockito.doReturn(mockData)
                .when(spyService)
                .mockCoreHistory("123-456", start, end);

        // when
        List<AccountHistoryRes> result = spyService.getHistory(userId, req, userContext);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("T1");
        assertThat(result.get(0).amount()).isEqualTo("50,000");
    }

    /**
     * ===============================
     *  2. 거래내역이 없을 경우 빈 리스트
     * ===============================
     */
    @Test
    @DisplayName("거래내역 조회 - 해당 월에 거래가 없으면 빈 리스트 반환")
    void getHistory_emptyList() {
        Long userId = 10L;

        AccountHistoryReq req = new AccountHistoryReq(AccountType.ALLOWANCE, 2025, 2);

        Account account = Account.builder()
                .accountNo("123-456")
                .build();

        given(userContext.getId()).willReturn(userId);
        given(userContext.getRole()).willReturn(Role.CHILD.name());

        given(accountRepository.findByUserIdAndType(eq(userId), eq(AccountType.ALLOWANCE)))
                .willReturn(Optional.of(account));

        AccountHistoryServiceImpl spyService = Mockito.spy(service);

        LocalDateTime start = LocalDateTime.of(2025, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 2, 28, 23, 59, 59);

        Mockito.doReturn(List.of())
                .when(spyService)
                .mockCoreHistory("123-456", start, end);

        List<AccountHistoryRes> result = spyService.getHistory(userId, req, userContext);

        assertThat(result).isEmpty();
    }

    /**
     * ===============================
     *  3. 부모가 남의 자녀 계좌 조회 → UNAUTHORIZED
     * ===============================
     */
    @Test
    @DisplayName("거래내역 조회 - 부모가 자신의 자녀가 아닌 사람의 계좌 조회 시 UNAUTHORIZED")
    void getHistory_parentUnauthorized_fail() {

        Long parentId = 1L;
        Long targetUserId = 999L;   // 내 자녀가 아님

        given(userContext.getId()).willReturn(parentId);
        given(userContext.getRole()).willReturn(Role.PARENT.name());
        given(userContext.getChildren()).willReturn(List.of(10L));  // 내 자녀는 10

        AccountHistoryReq req = new AccountHistoryReq(AccountType.ALLOWANCE, 2025, 1);

        assertThatThrownBy(() ->
                service.getHistory(targetUserId, req, userContext)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorBaseCode.UNAUTHORIZED);
    }
}
