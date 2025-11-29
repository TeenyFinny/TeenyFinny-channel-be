package dev.syntax.domain.account.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.AccountBalanceRes;
import dev.syntax.domain.account.dto.AccountSummaryRes;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreUserAccountListRes;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.card.repository.CardRepository;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import dev.syntax.global.service.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountBalanceServiceImpl implements AccountBalanceService {

    private final AccountRepository accountRepository; 
    private final CardRepository cardRepository;
    private final CoreAccountClient coreAccountClient;

    /**
     * 전체 계좌 잔액 요약 조회
     */
    @Override
    public AccountSummaryRes getSummary(UserContext ctx, Long targetUserId) {

        validateAccess(ctx, targetUserId);

        CoreUserAccountListRes res = coreAccountClient.getUserAccounts();
        List<CoreAccountItemRes> accounts = extractTargetAccounts(res, targetUserId);

        log.info("accounts: {}", res);
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal allowance = BigDecimal.valueOf(-1);
        BigDecimal invest = BigDecimal.valueOf(-1);
        BigDecimal goal = BigDecimal.valueOf(-1);

        for (CoreAccountItemRes acc : accounts) {
            AccountType type = acc.accountType();
            BigDecimal balance = acc.balance();

            switch (type) {
                case ALLOWANCE -> allowance = balance;
                case INVEST -> invest = balance;
                case GOAL -> goal = balance;
            }
            total = total.add(balance);
        }

        boolean hasCard = false;
        
        // ALLOWANCE 계좌 찾기 (카드 연결 확인용)
        Long allowanceAccountId = accounts.stream()
                .filter(a -> a.accountType() == AccountType.ALLOWANCE)
                .findFirst()
                .map(CoreAccountItemRes::accountId)
                .orElse(null);

        if (allowanceAccountId != null) {
            hasCard = cardRepository.existsByAccountId(allowanceAccountId);
        }

        return new AccountSummaryRes(
                format(total),
                format(allowance),
                format(invest),
                format(goal),
                new AccountSummaryRes.CardInfo(hasCard)
        );
    }

    /**
     * 특정 계좌 유형 조회 (잔액 단일 조회)
     */
    @Override
    public AccountBalanceRes getBalance(UserContext ctx, Long targetUserId, AccountType type) {

        validateAccess(ctx, targetUserId);

        CoreUserAccountListRes res = coreAccountClient.getUserAccounts();
        List<CoreAccountItemRes> accounts = extractTargetAccounts(res, targetUserId);

        BigDecimal balance = accounts.stream()
                .filter(a -> a.accountType() == type)
                .findFirst()
                .map(CoreAccountItemRes::balance)
                .orElse(BigDecimal.ZERO);

        return new AccountBalanceRes(format(balance));
    }

    /**
     * 응답값 포맷팅
     */
    private String format(BigDecimal amount) {
        return Utils.NumberFormattingService(amount.intValue());
    }

    /**
     * 대상 사용자 계좌 목록 추출
     */
    private List<CoreAccountItemRes> extractTargetAccounts(CoreUserAccountListRes res, Long targetUserId) {

        if (res.children() != null) {
            for (var child : res.children()) {
                if (child.userId().equals(targetUserId)) {
                    return child.accounts();
                }
            }
        }

        return res.accounts(); // 요청자 본인 계좌
    }

    /**
     * 권한 검증
     */
    private void validateAccess(UserContext ctx, Long targetUserId) {

        if (ctx.getId().equals(targetUserId)) return;

        if (ctx.getRole().equals(Role.PARENT.name()) &&
                ctx.getChildren().contains(targetUserId)) return;

        throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
    }
}
