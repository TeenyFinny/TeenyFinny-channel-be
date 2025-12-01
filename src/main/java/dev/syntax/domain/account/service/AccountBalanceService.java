package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountBalanceRes;
import dev.syntax.domain.account.dto.AccountSummaryRes;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.global.auth.dto.UserContext;

/**
 * 계좌 요약 정보 조회 서비스 인터페이스.
 * <p>
 * 사용자의 총 자산, 계좌 유형별 잔액(용돈, 투자, 저축), 카드 보유 여부 등을
 * 종합적으로 조회하는 기능을 제공합니다.
 * </p>
 */
public interface AccountBalanceService {

    /**
     * 계좌 요약 정보 조회.
     *
     * @param ctx          로그인한 사용자 컨텍스트 (권한 검증용)
     * @param targetUserId 조회 대상 사용자 ID (자녀 ID)
     * @return 계좌 요약 정보 응답 DTO (총 자산, 유형별 잔액, 카드 정보)
     */
    AccountSummaryRes getSummary(UserContext ctx, Long targetUserId);

    // 특정 계좌 타입의 잔액만 조회
    AccountBalanceRes getBalance(UserContext ctx, Long targetUserId, AccountType type);
}
