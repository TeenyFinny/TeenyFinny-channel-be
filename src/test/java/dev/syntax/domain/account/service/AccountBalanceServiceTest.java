package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountSummaryRes;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreChildAccountInfoRes;
import dev.syntax.domain.account.dto.core.CoreUserAccountListRes;
import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.AccountBalanceRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.card.repository.CardRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

   @InjectMocks
   private AccountBalanceServiceImpl accountSummaryService;

   @Mock
   private AccountRepository accountRepository;

   @Mock
   private CardRepository cardRepository;

   @Mock
   private CoreAccountClient coreAccountClient;

   @Mock
   private UserRepository userRepository;

       /**
     * TC-ACCOUNT-003: 자녀 본인 계좌 잔액 조회 성공
     *
     * <p>테스트 시나리오:</p>
     * <ul>
     *     <li>부모가 특정 자녀의 계좌 요약 정보를 조회한다</li>
     *     <li>부모-자녀 관계(UserRelationship) 검증</li>
     *     <li>채널 DB(UserRepository)에서 해당 자녀가 존재하는지 확인</li>
     *     <li>Core Banking API(getUserAccounts)를 호출하여 자녀의 모든 계좌 정보 조회</li>
     *     <li>ALLOWANCE / INVEST / GOAL 잔액을 각각 계산</li>
     *     <li>총 자산(total)을 계산하여 AccountSummaryRes 형태로 반환</li>
     *     <li>ALLOWANCE 계좌와 매핑된 카드가 존재하는지(cardRepository.existsByAccountId) 확인 후 card.hasCard 설정</li>
     *     <li>최종적으로 정상적인 요약 정보가 반환되어야 한다</li>
     * </ul>
     *
     * <p>API 요구사항:</p>
     * <ul>
     *     <li><b>GET /account/summary</b></li>
     *     <li>요청한 자녀(childId)의 계좌 요약(total, allowance, invest, goal, card 보유 여부)을 반환</li>
     * </ul>
     */

   @Test
   @DisplayName("계좌 요약 조회 성공 케이스")
   void getSummary_Success() {
        // given - 부모, 자녀, 관계 정보
        User parent = User.builder().id(1L).role(Role.PARENT).build();
        User child = User.builder().id(2L).role(Role.CHILD).coreUserId(2L).build();

        UserRelationship childRelationship = UserRelationship.builder()
                .child(child)
                .parent(parent)
                .build();

        parent.getChildren().add(childRelationship);
        UserContext ctx = new UserContext(parent);

        when(userRepository.findById(2L)).thenReturn(Optional.of(child));

        // Core에서 반환하는 자녀 계좌 목록
        List<CoreAccountItemRes> childAccounts = List.of(
                new CoreAccountItemRes(1L, "ALL-01", AccountType.ALLOWANCE, new BigDecimal("50000")),
                new CoreAccountItemRes(2L, "INV-01", AccountType.INVEST, new BigDecimal("30000")),
                new CoreAccountItemRes(3L, "GOAL-01", AccountType.GOAL, new BigDecimal("20000"))
        );
        
        // 자녀 계좌 정보를 포함한 Core 응답 생성
        CoreChildAccountInfoRes childInfo = new CoreChildAccountInfoRes(2L, childAccounts);
        
        when(coreAccountClient.getUserAccounts())
                .thenReturn(new CoreUserAccountListRes(List.of(), List.of(childInfo)));

        // 카드 보유 여부 Mock
        when(accountRepository.findByUserIdAndType(2L, AccountType.ALLOWANCE))
                .thenReturn(Optional.of(Account.builder().id(10L).build()));
        when(cardRepository.existsByAccountId(10L)).thenReturn(true);

        // when
        AccountSummaryRes res = accountSummaryService.getSummary(ctx, 2L);

        // then
        assertThat(res.getTotal()).isEqualTo("100,000");
        assertThat(res.getAllowance()).isEqualTo("50,000");
        assertThat(res.getInvest()).isEqualTo("30,000");
        assertThat(res.getGoal()).isEqualTo("20,000");
        assertThat(res.getCard().isHasCard()).isTrue();
   }
}
