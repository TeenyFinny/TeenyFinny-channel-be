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
    private final CoreAccountClient coreAccountClient;

    /**
     * 전체 계좌 잔액 요약 조회
     */
    @Override
    public AccountSummaryRes getSummary(UserContext ctx, Long targetUserId) {

        validateAccess(ctx, targetUserId);

        // 각 타입별 계좌 조회
        Optional<Account> allowanceAccount = accountRepository.findByUserIdAndType(targetUserId, AccountType.ALLOWANCE);
        Optional<Account> investAccount = accountRepository.findByUserIdAndType(targetUserId, AccountType.INVEST);
        Optional<Account> goalAccount = accountRepository.findByUserIdAndType(targetUserId, AccountType.GOAL);

        // Core API로 잔액 조회
        CoreUserAccountListRes coreRes = coreAccountClient.getUserAccounts();
        Map<String, BigDecimal> balanceMap = buildBalanceMap(coreRes, targetUserId);

        // 각 타입별 잔액 설정 (계좌 없으면 -1, 있으면 잔액)
        String allowanceBalance = allowanceAccount
                .map(acc -> formatBalanceFromCore(balanceMap, acc.getAccountNo()))
                .orElse("-1");

        String investBalance = investAccount
                .map(acc -> formatBalanceFromCore(balanceMap, acc.getAccountNo()))
                .orElse("-1");

        String goalBalance = goalAccount
                .map(acc -> formatBalanceFromCore(balanceMap, acc.getAccountNo()))
                .orElse("-1");

        // total 계산 (DEPOSIT 제외, -1이 아닌 것만)
        BigDecimal total = BigDecimal.ZERO;
        if (allowanceAccount.isPresent()) {
            total = total.add(balanceMap.getOrDefault(allowanceAccount.get().getAccountNo(), BigDecimal.ZERO));
        }
        if (investAccount.isPresent()) {
            total = total.add(balanceMap.getOrDefault(investAccount.get().getAccountNo(), BigDecimal.ZERO));
        }
        if (goalAccount.isPresent()) {
            total = total.add(balanceMap.getOrDefault(goalAccount.get().getAccountNo(), BigDecimal.ZERO));
        }

        // 카드 보유 여부 확인
        boolean hasCard = allowanceAccount
                .map(acc -> cardRepository.existsByAccountId(acc.getId()))
                .orElse(false);

        return new AccountSummaryRes(
                format(total),
                allowanceBalance,
                investBalance,
                goalBalance,
                new AccountSummaryRes.CardInfo(hasCard)
        );
    }

    /**
     * Core 응답에서 계좌번호별 잔액 맵을 생성합니다.
     */
    private Map<String, BigDecimal> buildBalanceMap(CoreUserAccountListRes coreRes, Long targetUserId) {
        List<CoreAccountItemRes> accounts = extractTargetAccounts(coreRes, targetUserId);
        return accounts.stream()
                .collect(Collectors.toMap(
                        CoreAccountItemRes::accountNumber,
                        acc -> acc.balance() != null ? acc.balance() : BigDecimal.ZERO,
                        (a, b) -> a
                ));
    }

    /**
     * 계좌번호로 Core 잔액을 조회하여 포맷팅합니다.
     */
    private String formatBalanceFromCore(Map<String, BigDecimal> balanceMap, String accountNo) {
        BigDecimal balance = balanceMap.getOrDefault(accountNo, BigDecimal.ZERO);
        return format(balance);
    }

    /**
     * 계좌 타입별로 잔액을 그룹핑합니다.
     * <p>
     * 동일한 계좌 타입의 잔액들을 합산하여 맵으로 반환합니다.
     * </p>
     *
     * @param accounts 계좌 정보 리스트
     * @return 계좌 타입 -> 총 잔액 맵
     */
    private Map<AccountType, BigDecimal> groupBalancesByAccountType(List<CoreAccountItemRes> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return Map.of();
        }
        return accounts.stream()
                .filter(account -> account.accountType() != null)
                .collect(Collectors.groupingBy(
                        CoreAccountItemRes::accountType,
                        Collectors.reducing(BigDecimal.ZERO, 
                                acc -> acc.balance() != null ? acc.balance() : BigDecimal.ZERO, 
                                BigDecimal::add)
                ));
    }

    /**
     * 특정 계좌 타입의 잔액을 포맷팅하여 반환합니다.
     * <p>
     * 해당 타입의 계좌가 없으면 "-1"을 반환합니다.
     * 계좌가 있으면 잔액을 포맷팅하여 반환합니다 (0 포함).
     * </p>
     *
     * @param balancesByType 계좌 타입별 잔액 맵
     * @param accountType 조회할 계좌 타입
     * @return 포맷팅된 잔액 문자열 (예: "120,000" 또는 "-1")
     */
    private String formatBalance(Map<AccountType, BigDecimal> balancesByType, AccountType accountType) {
        if (!balancesByType.containsKey(accountType)) {
            return "-1";
        }
        return format(balancesByType.get(accountType));
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
