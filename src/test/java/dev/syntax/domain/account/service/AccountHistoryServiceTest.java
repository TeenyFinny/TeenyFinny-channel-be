package dev.syntax.domain.account.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.AccountHistoryReq;
import dev.syntax.domain.account.dto.AccountHistoryRes;
import dev.syntax.domain.account.dto.core.CoreTransactionHistoryRes;
import dev.syntax.domain.account.dto.core.CoreTransactionDetailItemRes;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountHistoryServiceTest {

    @InjectMocks
    private AccountHistoryServiceImpl service;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CoreAccountClient coreAccountClient;

    @Mock
    private UserContext userContext;

    /**
     * ===============================
     *  2. 거래내역이 없을 경우 빈 리스트
     * ===============================
     */
    @Test
    @DisplayName("거래내역 조회 - 해당 월에 거래가 없으면 빈 리스트 반환")
    void getHistory_emptyList() {
        Long userId = 10L;
        String accountNo = "123-456";

        AccountHistoryReq req = new AccountHistoryReq(AccountType.ALLOWANCE, 2025, 2);

        Account account = Account.builder()
                .accountNo(accountNo)
                .build();

        given(userContext.getId()).willReturn(userId);
        given(userContext.getRole()).willReturn(Role.CHILD.name());

        given(accountRepository.findByUserIdAndType(eq(userId), eq(AccountType.ALLOWANCE)))
                .willReturn(Optional.of(account));

        // Core 서버 응답 Mock (빈 리스트)
        CoreTransactionHistoryRes coreRes = new CoreTransactionHistoryRes(List.of(), new BigDecimal("0"));

        given(coreAccountClient.getAccountTransactionsByMonth(accountNo, 2025, 2))
                .willReturn(coreRes);

        // when
        List<AccountHistoryRes> result = service.getHistory(userId, req, userContext);

        // then
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
