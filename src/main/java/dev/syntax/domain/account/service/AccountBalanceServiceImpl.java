package dev.syntax.domain.account.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import dev.syntax.domain.account.dto.AccountBalanceRes;
import dev.syntax.domain.account.dto.AccountSummaryRes;
import dev.syntax.domain.account.entity.Account;
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

    /**
     * 사용자 또는 자녀의 전체 계좌 요약 조회.
     * Core 연동 전이므로 서비스에서 계좌 존재 여부만 확인하고
     * 잔액은 Mock 데이터로 채움.
     */
    @Override
    public AccountSummaryRes getSummary(UserContext ctx, Long targetUserId) {

        log.info("[AccountSummary] 요청 userId={}, targetUserId={}", ctx.getId(), targetUserId);

        // 🔐 접근 권한 체크
        validateAccess(ctx, targetUserId);

        // ===== 1. 계좌 존재 여부 확인 =====
        Account allowanceAcc = accountRepository
                .findByUserIdAndType(targetUserId, AccountType.ALLOWANCE)
                .orElse(null);

        Account investAcc = accountRepository
                .findByUserIdAndType(targetUserId, AccountType.INVEST)
                .orElse(null);

        Account savingAcc = accountRepository
                .findByUserIdAndType(targetUserId, AccountType.GOAL)
                .orElse(null);

        // ===== 2. Mock 잔액 생성 =====
        BigDecimal allowance = (allowanceAcc != null)
                ? mockBalance(allowanceAcc.getId(), AccountType.ALLOWANCE)
                : BigDecimal.ZERO;

        BigDecimal invest = (investAcc != null)
                ? mockBalance(investAcc.getId(), AccountType.INVEST)
                : BigDecimal.ZERO;

        BigDecimal saving = (savingAcc != null)
                ? mockBalance(savingAcc.getId(), AccountType.GOAL)
                : BigDecimal.ZERO;

        BigDecimal total = allowance.add(invest).add(saving);

        // ===== 3. 카드 보유 여부 =====
        boolean hasCard = allowanceAcc != null &&
                cardRepository.existsByAccountId(allowanceAcc.getId());

        return new AccountSummaryRes(
                format(total),
                format(allowance),
                format(invest),
                format(saving),
                new AccountSummaryRes.CardInfo(hasCard)
        );
    }

    /**
     * 특정 계좌 타입의 잔액 조회.
     */
    @Override
    public AccountBalanceRes getBalance(UserContext ctx, Long targetUserId, AccountType type) {

        validateAccess(ctx, targetUserId);

        Account account = accountRepository
                .findByUserIdAndType(targetUserId, type)
                .orElse(null);

        BigDecimal balance = (account != null)
                ? mockBalance(account.getId(), type)
                : BigDecimal.ZERO;

        return new AccountBalanceRes(format(balance));
    }

    /**
     * BigDecimal → "12,000" 문자열 변환
     */
    private String format(BigDecimal amount) {
        return Utils.NumberFormattingService(amount.intValue());
    }

    /**
     * 🔐 접근 권한 검증
     */
    private void validateAccess(UserContext ctx, Long targetUserId) {

        Long currentUserId = ctx.getId();

        // 본인 → 허용
        if (currentUserId.equals(targetUserId)) return;

        // 부모 → 자녀 허용
        if (ctx.getRole().equals(Role.PARENT.name())
                && ctx.getChildren().contains(targetUserId)) return;

        throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
    }

    /**
     * 🧪 Mock 잔액 생성 (테스트용)
     */
    private BigDecimal mockBalance(Long accountId, AccountType type) {

        long base = accountId % 50000;

        switch (type) {
            case ALLOWANCE:
                return BigDecimal.valueOf(10000 + base);
            case INVEST:
                return BigDecimal.valueOf(50000 + base);
            case GOAL:
                return BigDecimal.valueOf(20000 + base);
            default:
                return BigDecimal.ZERO;
        }
    }
}
