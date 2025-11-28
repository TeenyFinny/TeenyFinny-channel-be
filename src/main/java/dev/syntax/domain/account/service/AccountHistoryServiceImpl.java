package dev.syntax.domain.account.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.AccountHistoryReq;
import dev.syntax.domain.account.dto.AccountHistoryRes;
import dev.syntax.domain.account.dto.core.CoreTransactionHistoryRes;
import dev.syntax.domain.account.dto.core.CoreTransactionItemRes;
import dev.syntax.domain.account.dto.core.CoreTransactionDetailItemRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import dev.syntax.global.service.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 계좌 거래내역 조회 서비스 구현체.
 *
 * UserContext를 기반으로 부모/자녀 권한을 체크하고,
 * 코어 뱅킹 서버(CoreAccountClient)에서 거래내역을 조회하여
 * 프론트가 요구하는 형태(AccountHistoryRes)로 변환한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountHistoryServiceImpl implements AccountHistoryService {

    private final AccountRepository accountRepository;
    private final CoreAccountClient coreAccountClient;

    @Override
    public List<AccountHistoryRes> getHistory(Long userId, AccountHistoryReq req, UserContext ctx) {
        // 1. 부모/자녀 인가 체크
        validateUserAccess(userId, ctx);

        // 2. 계좌 존재 확인
        Account account = accountRepository
                .findByUserIdAndType(userId, req.accountType())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));

        // 3. Core 서버 호출
        CoreTransactionHistoryRes coreRes = coreAccountClient.getAccountTransactionsByMonth(
                account.getAccountNo(), req.year(), req.month());
        if (coreRes == null || coreRes.transactions() == null) {
            throw new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY);
        }

        // 4. 응답 변환 (Core → Channel)
        return convertToAccountHistoryRes(coreRes.transactions());
    }

    private void validateUserAccess(Long targetUserId, UserContext ctx) {

        if (Role.CHILD.name().equals(ctx.getRole())) {
            if (!ctx.getId().equals(targetUserId)) {
                throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
            }
            return;
        }

        // [수정] 부모가 본인을 조회하거나, 자녀를 조회하는 경우 허용
        if (ctx.getId().equals(targetUserId)) {
            return;
        }

        if (!ctx.getChildren().contains(targetUserId)) {
            throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
        }
    }

    /**
     * Core 서버 응답을 Channel 응답 형식으로 변환합니다.
     * <p>
     * - amount가 양수면 "deposit", 음수면 "withdrawal"
     * - 금액은 절대값으로 변환하고 천단위 콤마 적용
     * - 날짜는 "yyyy-MM-dd HH:mm" 형식으로 변환
     * </p>
     */
    private List<AccountHistoryRes> convertToAccountHistoryRes(List<CoreTransactionItemRes> items) {
        return items.stream()
                .map(item -> new AccountHistoryRes(
                        item.transactionId(),
                        item.amount().compareTo(BigDecimal.ZERO) >= 0 ? "deposit" : "withdrawal",
                        item.merchantName(),
                        Utils.NumberFormattingService(item.amount().abs()), // 프론트에서 문자열 원함
                        Utils.NumberFormattingService(item.balanceAfter()), // 프론트에서 문자열 원함
                        item.category().getKoreanName(),
                        item.transactionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) // timestamp로 전달
                ))
                .toList();
    }

}