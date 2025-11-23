package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountHistoryReq;
import dev.syntax.domain.account.dto.AccountHistoryRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.account.service.AccountHistoryServiceImpl;
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
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AccountHistoryServiceTest {
    @InjectMocks
    private AccountHistoryServiceImpl accountHistoryServiceImpl;
    @Mock
    private AccountRepository accountRepository;

    @Test
    @DisplayName("자녀가 본인의 계좌를 조회하면 성공한다")
    void getHistory_Child_Self_Success() {
        // given
        Long childId = 10L;
        UserContext childCtx = mock(UserContext.class);
        given(childCtx.getId()).willReturn(childId);
        given(childCtx.getRole()).willReturn(Role.CHILD.name());
        AccountHistoryReq req = new AccountHistoryReq(AccountType.ALLOWANCE, 2025, 10);
        Account account = Account.builder().accountNo("123-456").build();
        given(accountRepository.findByUserIdAndType(eq(childId), any()))
                .willReturn(Optional.of(account));
        // when
        List<AccountHistoryRes> result = accountHistoryServiceImpl.getHistory(childId, req, childCtx);
        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).amount()).isEqualTo("50,000"); // Mock Data 확인
    }

    @Test
    @DisplayName("부모가 자녀의 계좌를 조회하면 성공한다")
    void getHistory_Parent_Child_Success() {
        // given
        Long parentId = 1L;
        Long childId = 10L;
        UserContext parentCtx = mock(UserContext.class);
        given(parentCtx.getId()).willReturn(parentId);
        given(parentCtx.getRole()).willReturn(Role.PARENT.name());
        given(parentCtx.getChildren()).willReturn(List.of(childId)); // 자녀 목록에 포함됨
        AccountHistoryReq req = new AccountHistoryReq(AccountType.ALLOWANCE, 2025, 10);
        Account account = Account.builder().accountNo("123-456").build();
        given(accountRepository.findByUserIdAndType(eq(childId), any()))
                .willReturn(Optional.of(account));
        // when
        List<AccountHistoryRes> result = accountHistoryServiceImpl.getHistory(childId, req, parentCtx);
        // then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("부모가 내 자녀가 아닌 다른 사람의 계좌를 조회하면 실패한다")
    void getHistory_Parent_Other_Fail() {
        // given
        Long parentId = 1L;
        Long otherId = 99L;
        UserContext parentCtx = mock(UserContext.class);
        given(parentCtx.getId()).willReturn(parentId);
        given(parentCtx.getRole()).willReturn(Role.PARENT.name());
        given(parentCtx.getChildren()).willReturn(List.of(10L)); // 다른 자녀만 있음
        AccountHistoryReq req = new AccountHistoryReq(AccountType.ALLOWANCE, 2025, 10);
        // when & then
        assertThatThrownBy(() -> accountHistoryServiceImpl.getHistory(otherId, req, parentCtx))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorBaseCode.UNAUTHORIZED);
    }
}