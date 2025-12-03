package dev.syntax.domain.account.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.AccountBalanceRes;
import dev.syntax.domain.account.dto.AccountSummaryRes;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreUserAccountListRes;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.card.repository.CardRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import dev.syntax.global.service.BalanceProvider;
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
    private final BalanceProvider balanceProvider;
    private final UserRepository userRepository;

    /**
     * 특정 자녀의 계좌 잔액 요약 조회 (부모가 자녀 계좌 조회)
     */
    @Override
    public AccountSummaryRes getSummary(UserContext ctx, Long targetUserId) {

        // 1. 부모 권한 검증
        validateParentAccess(ctx, targetUserId);

        // 2. 자녀 정보 조회 (CoreUserId 필요)
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));

        log.info("=== 자녀 계좌 조회 - parentId: {}, childId: {}, coreUserId: {} ===", 
                ctx.getId(), targetUserId, targetUser.getCoreUserId());

        // 3. Core API에서 전체 계좌 정보 조회
        CoreUserAccountListRes coreAccounts = coreAccountClient.getUserAccounts();
        
        // 4. 대상 자녀의 계좌 목록 추출 (CoreUserId 기준)
        List<CoreAccountItemRes> targetAccounts = extractTargetAccounts(coreAccounts, targetUser.getCoreUserId());
        
        if (targetAccounts == null) {
            log.warn("Core API 응답에서 해당 자녀(coreUserId={})의 계좌 정보를 찾을 수 없습니다.", targetUser.getCoreUserId());
            targetAccounts = List.of();
        }

        log.info("자녀 계좌 수: {}, 계좌 타입: {}", 
                targetAccounts.size(), 
                targetAccounts.stream().map(CoreAccountItemRes::accountType).collect(Collectors.toList()));

        // 5. 계좌 타입별로 그룹핑
        Map<AccountType, BigDecimal> balancesByType = groupBalancesByAccountType(targetAccounts);

        // 6. 각 타입별 잔액 설정
        String allowanceBalance = formatBalance(balancesByType, AccountType.ALLOWANCE);
        String investBalance = formatBalance(balancesByType, AccountType.INVEST);
        String goalBalance = formatBalance(balancesByType, AccountType.GOAL);

        // 7. total 계산 (DEPOSIT 제외)
        BigDecimal totalAmount = targetAccounts.stream()
                .filter(acc -> acc.accountType() != AccountType.DEPOSIT)
                .map(CoreAccountItemRes::balance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 8. 카드 보유 여부 확인 (Channel DB 기준)
        boolean hasCard = accountRepository.findByUserIdAndType(targetUserId, AccountType.ALLOWANCE)
                .map(acc -> cardRepository.existsByAccountId(acc.getId()))
                .orElse(false);

        log.info("조회 결과 - Total: {}, ALLOWANCE: {}, INVEST: {}, GOAL: {}, HasCard: {}", 
                format(totalAmount), allowanceBalance, investBalance, goalBalance, hasCard);

        return new AccountSummaryRes(
                format(totalAmount),
                allowanceBalance,
                investBalance,
                goalBalance,
                new AccountSummaryRes.CardInfo(hasCard)
        );
    }

    /**
     * 특정 계좌 유형 조회 (잔액 단일 조회)
     */
    @Override
    public AccountBalanceRes getBalance(UserContext ctx, Long targetUserId, AccountType type) {

        validateParentAccess(ctx, targetUserId); // Use the new validation method

        // User 엔티티 조회
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));

        // BalanceProvider를 사용하여 특정 타입 잔액 조회
        long balance = balanceProvider.getUserBalanceByType(targetUser, type);

        return new AccountBalanceRes(format(BigDecimal.valueOf(balance)));
    }

    /**
     * 응답값 포맷팅
     */
    private String format(BigDecimal amount) {
        return Utils.NumberFormattingService(amount);
    }

    /**
     * 대상 사용자 계좌 목록 추출 (CoreUserId 기준)
     */
    private List<CoreAccountItemRes> extractTargetAccounts(CoreUserAccountListRes res, Long targetCoreUserId) {
        if (res.children() == null) {
            return List.of();
        }
        return res.children().stream()
                .filter(child -> child.userId().equals(targetCoreUserId))
                .findFirst()
                .map(child -> Optional.ofNullable(child.accounts()).orElse(List.of()))
                .orElse(List.of());
    }

    /**
     * 계좌 타입별로 잔액을 그룹핑 (HomeService 로직)
     */
    private Map<AccountType, BigDecimal> groupBalancesByAccountType(List<CoreAccountItemRes> accounts) {
        if (accounts == null) {
            return Map.of();
        }
        return accounts.stream()
                .filter(account -> account.accountType() != null)
                .collect(Collectors.groupingBy(
                        CoreAccountItemRes::accountType,
                        Collectors.reducing(BigDecimal.ZERO, CoreAccountItemRes::balance, BigDecimal::add)
                ));
    }

    /**
     * 특정 계좌 타입의 잔액을 포맷팅하여 반환 (HomeService 로직)
     * 계좌가 없으면 "-1" 반환
     */
    private String formatBalance(Map<AccountType, BigDecimal> balancesByType, AccountType accountType) {
        if (!balancesByType.containsKey(accountType)) {
            return "-1";
        }
        return format(balancesByType.get(accountType));
    }

    /**
     * 부모 권한 검증 (부모만 접근 가능)
     */
    private void validateParentAccess(UserContext ctx, Long targetUserId) {

        // 부모 권한 확인
        if (!ctx.getRole().equals(Role.PARENT.name())) {
            throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
        }

        // 자녀 목록에 포함되어 있는지 확인
        if (!ctx.getChildren().contains(targetUserId)) {
            throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
        }

        log.info("부모 권한 검증 통과 - parentId: {}, childId: {}", ctx.getId(), targetUserId);
    }
}
