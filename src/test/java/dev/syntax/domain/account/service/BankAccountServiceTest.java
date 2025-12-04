package dev.syntax.domain.account.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.CreateChildAccountReq;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreInvestmentAccountRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.service.InvestAccountService;
import dev.syntax.domain.investment.service.InvestAccountServiceImpl;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {
    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @InjectMocks
    private InvestAccountServiceImpl investAccountService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CoreAccountClient coreAccountClient;

    @Mock
    private CoreInvestmentClient coreInvestmentClient;

    /**
     * TC-ACCOUNT-001: 부모가 자녀 용돈 계좌 생성 성공
     * 
     * <p>테스트 시나리오:</p>
     * <ul>
     *   <li>부모 사용자가 자녀의 용돈 계좌 생성 요청</li>
     *   <li>자녀 존재 여부 및 부모-자녀 관계 검증</li>
     *   <li>이미 존재하는 용돈 계좌가 없는지 확인</li>
     *   <li>Core Banking API를 통해 계좌 생성</li>
     *   <li>생성된 계좌 정보를 DB에 저장</li>
     * </ul>
     */
    @Test
    @DisplayName("부모가 자녀 용돈 계좌 생성 성공 시 계좌가 저장된다")
    void createChildAllowanceAccount_success() {
        // given
        User parent = User.builder()
                .id(1L)
                .role(Role.PARENT)
                .coreUserId(1234L) // Long 타입이어야 함
                .build();

        User child = User.builder()
                .id(2L)
                .role(Role.CHILD)
                .coreUserId(2345L) // Long 타입이어야 함
                .build();

        // 부모 - 자녀 관계 설정 (UserRelationship 추가)
        UserRelationship relationship = UserRelationship.builder()
                .parent(parent)
                .child(child)
                .build();
        parent.getChildren().add(relationship);

        // CreateChildAccountReq 생성자 수정 (모든 필드 포함)
        CreateChildAccountReq req = new CreateChildAccountReq(
            2L, "자녀", "010-1234-5678", "2010-01-01", "서울", "강남구"
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(child));
        when(accountRepository.findByUserIdAndType(2L, AccountType.ALLOWANCE))
                .thenReturn(Optional.empty());

        CoreAccountItemRes coreResponse = new CoreAccountItemRes(
                1L,
                "01010123456789",
                AccountType.ALLOWANCE,
                new BigDecimal("100000")
        );

        when(coreAccountClient.createChildAccount(any())).thenReturn(coreResponse);

        // when
        bankAccountService.createChildAllowanceAccount(parent, req);

        // then
        verify(userRepository).findById(2L);
        verify(coreAccountClient).createChildAccount(any());
        verify(accountRepository).save(any(Account.class));
    }

        /**
     * TC-ACCOUNT-002: 부모가 자녀 투자 계좌 생성 성공
     *
     * <p>테스트 시나리오:</p>
     * <ul>
     *     <li>부모가 특정 자녀의 투자 계좌 생성 요청</li>
     *     <li>채널 DB에서 해당 자녀의 투자 계좌가 이미 존재하는지 확인 → 존재하지 않음</li>
     *     <li>Core Investment API를 호출하여 투자 계좌를 생성</li>
     *     <li>Core 응답에 계좌번호가 포함되어 있는지 검증</li>
     *     <li>채널 DB(UserRepository)에서 자녀 정보를 조회</li>
     *     <li>Account 엔티티를 생성하여 투자 계좌 정보를 채널 DB에 저장</li>
     *     <li>정상 흐름일 경우 예외 없이 성공적으로 계좌 저장이 이루어져야 함</li>
     * </ul>
     */

    @Test
    @DisplayName("부모가 자녀 투자 계좌 생성 성공 시 계좌가 저장된다")
    void createChildInvestAccount_success() {
        // given
        Long testUserId = 10L;
        String testAccountNo = "01010123456789";

        given(accountRepository.findByUserIdAndType(testUserId, AccountType.INVEST))
                .willReturn(Optional.empty());

        CoreInvestmentAccountRes mockRes = new CoreInvestmentAccountRes(
                testAccountNo,
                testUserId,
                100000L
        );

        given(coreInvestmentClient.createInvestmentAccount(testUserId))
                .willReturn(mockRes);

        User mockUser = User.builder().id(testUserId).build();
        given(userRepository.findById(testUserId))
                .willReturn(Optional.of(mockUser));

        // when
        assertDoesNotThrow(() -> investAccountService.createInvestmentAccount(testUserId));

        // then
        then(coreInvestmentClient).should(times(1)).createInvestmentAccount(testUserId);
        then(accountRepository).should(times(1)).save(any(Account.class));
    }
   
}

