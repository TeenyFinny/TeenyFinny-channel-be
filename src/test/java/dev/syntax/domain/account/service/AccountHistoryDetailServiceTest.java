package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountHistoryDetailRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.entity.User;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AccountHistoryDetailServiceTest {

    @InjectMocks
    private AccountHistoryDetailServiceImpl service;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserContext userContext;

    /**
     * ===============================
     * 1. 정상 조회
     * ===============================
     */
    @Test
    @DisplayName("거래 상세 조회 - 정상 조회 성공")
    void getDetail_success() {
        // given
        Long transactionId = 202501150001L;
        Long accountIdByTx = 1L; // mock logic: 202501150001 % 1000 = 1
        Long userId = 10L;

        User user = mock(User.class);
        given(user.getId()).willReturn(userId);

        Account account = Account.builder()
                .id(accountIdByTx)
                .accountNo("123-456")
                .user(user)
                .build();

        // UserContext Mock
        given(userContext.getId()).willReturn(userId);
        given(userContext.getRole()).willReturn(Role.CHILD.name());

        // Account Repository Mock
        given(accountRepository.findById(accountIdByTx)).willReturn(Optional.of(account));

        // when
        AccountHistoryDetailRes res = service.getDetail(transactionId, userContext);

        // then
        assertThat(res).isNotNull();
        assertThat(res.merchant()).isEqualTo("이체");
    }

    /**
     * ===============================
     * 2. invalid transactionId
     * ===============================
     */
    @Test
    @DisplayName("거래 상세 조회 - transactionId가 잘못되면 예외 발생")
    void getDetail_invalid_transactionId() {
        // given
        Long invalidTxId = 0L;

        // when & then
        assertThatThrownBy(() -> service.getDetail(invalidTxId, userContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorBaseCode.TX_INVALID_TRANSACTION_ID);
    }

    /**
     * ===============================
     * 3. mock 상세 데이터 없음
     * ===============================
     */
    @Test
    @DisplayName("거래 상세 조회 - mockCoreDetail에 없으면 NOT_FOUND")
    void getDetail_notFound() {
        // given
        Long notExistTx = 999999999999L;

        // when & then
        assertThatThrownBy(() -> service.getDetail(notExistTx, userContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorBaseCode.TX_NOT_FOUND);
    }

    /**
     * ===============================
     * 4. 계좌 없음
     * ===============================
     */
    @Test
    @DisplayName("거래 상세 조회 - 계좌가 존재하지 않으면 ACCOUNT_NOT_FOUND")
    void getDetail_account_not_found() {
        // given
        Long txId = 202501150001L;
        Long accountIdByTx = 1L;

        // 계좌 없음
        given(accountRepository.findById(accountIdByTx)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.getDetail(txId, userContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorBaseCode.TX_ACCOUNT_NOT_FOUND);
    }

    /**
     * ===============================
     * 5. 권한 없음
     * ===============================
     */
    @Test
    @DisplayName("거래 상세 조회 - 권한 없는 사용자의 접근 차단")
    void getDetail_no_permission() {
        // given
        Long txId = 202501150001L;
        Long accountIdByTx = 1L;
        Long ownerId = 200L;
        Long otherUserId = 100L;

        User owner = mock(User.class);
        given(owner.getId()).willReturn(ownerId);

        Account account = Account.builder()
                .id(accountIdByTx)
                .accountNo("123-456")
                .user(owner) // 계좌 소유자
                .build();

        // 로그인 유저 (자녀, 소유자 아님)
        given(userContext.getId()).willReturn(otherUserId);
        given(userContext.getRole()).willReturn(Role.CHILD.name());

        given(accountRepository.findById(accountIdByTx)).willReturn(Optional.of(account));

        // when & then
        assertThatThrownBy(() -> service.getDetail(txId, userContext))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorBaseCode.TX_NO_PERMISSION);
    }
}
