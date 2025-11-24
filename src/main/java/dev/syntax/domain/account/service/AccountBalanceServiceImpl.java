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

        // 🔐 접근 권한 체크: 부모는 자신의 자녀만 조회 가능
        validateAccess(ctx, targetUserId);

        // ===== 1. 계좌 존재 여부 확인 =====
        Account allowanceAcc = accountRepository.findByUserIdAndType(targetUserId, AccountType.ALLOWANCE).orElse(null);
        Account investAcc = accountRepository.findByUserIdAndType(targetUserId, AccountType.INVEST).orElse(null);
        Account savingAcc = accountRepository.findByUserIdAndType(targetUserId, AccountType.GOAL).orElse(null);

        // ===== 2. Mock 잔액 생성 =====  
        // (원래는 Core API에서 가져와야 하지만 지금은 테스트 데이터로 대체)

        BigDecimal allowanceBalance = (allowanceAcc != null)
                ? mockBalance(allowanceAcc.getId(), AccountType.ALLOWANCE)
                : BigDecimal.ZERO;

        BigDecimal investBalance = (investAcc != null)
                ? mockBalance(investAcc.getId(), AccountType.INVEST)
                : BigDecimal.ZERO;

        BigDecimal savingBalance = (savingAcc != null)
                ? mockBalance(savingAcc.getId(), AccountType.GOAL)
                : BigDecimal.ZERO;

        // ===== 3. 총합 계산 =====
        BigDecimal total = allowanceBalance.add(investBalance).add(savingBalance);

        // ===== 4. 카드 보유 여부 체크 =====
        boolean hasCard = false;
        if (allowanceAcc != null) {
            hasCard = cardRepository.existsByAccountId(allowanceAcc.getId());
        }

        return new AccountSummaryRes(
                format(total),
                format(allowanceBalance),
                format(investBalance),
                format(savingBalance),
                new AccountSummaryRes.CardInfo(hasCard)
        );
    }

    /**
     * 특정 계좌 타입의 잔액 조회.
     */
    @Override
    public AccountBalanceRes getBalance(UserContext ctx, Long targetUserId, AccountType type) {
        // 1. 권한 체크
        validateAccess(ctx, targetUserId);

        // 2. 계좌 조회
        Account account = accountRepository.findByUserIdAndType(targetUserId, type).orElse(null);

        // 3. 잔액 Mocking
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
     * CHILD → 자기 자신만 조회 가능
     * PARENT → 자신의 자녀 조회 가능
     */
    private void validateAccess(UserContext ctx, Long targetUserId) {
        Long currentUserId = ctx.getId();

        // 본인 정보는 항상 접근 가능
        if (currentUserId.equals(targetUserId)) {
            return;
        }

        // 부모는 자녀 정보에 접근 가능
        if (ctx.getRole().equals(Role.PARENT.name()) && ctx.getChildren().contains(targetUserId)) {
            return;
        }

        // 그 외의 경우는 권한 없음
        throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
    }

    /**
     * 🧪 Mock 잔액 생성 로직
     * Core 연동 전 테스트용
     */
    private BigDecimal mockBalance(Long accountId, AccountType type) {

        // 계좌 ID 기반으로 잔액을 임의로 생성하는 방식 (테스트용)
        long base = accountId % 50000;   // 0~50000 사이 Random 값 흉내

        switch (type) {
            case ALLOWANCE:
                return BigDecimal.valueOf(10000 + base); // 최소 1만원
            case INVEST:
                return BigDecimal.valueOf(50000 + base);
            case GOAL:
                return BigDecimal.valueOf(20000 + base);
            default:
                return BigDecimal.ZERO;
        }
    }
}
    

