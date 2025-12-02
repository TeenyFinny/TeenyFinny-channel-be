package dev.syntax.domain.account.service;

import java.util.List;

import dev.syntax.domain.account.dto.AccountHistoryReq;
import dev.syntax.domain.account.dto.AccountHistoryRes;
import dev.syntax.global.auth.dto.UserContext;

/**
 * 계좌 거래내역 조회 서비스 인터페이스.
 *
 * <p>
 * 사용자(부모/자녀)가 특정 계좌에 대해 월별 거래내역을 조회하도록 제공하는 서비스이다.
 * childId(PathVariable)는 부모가 자녀 계좌를 조회하는 경우에만 제공되며,
 * 자녀(CHILD)라면 null을 전달한다.
 * </p>
 *
 * @see AccountHistoryReq
 * @see AccountHistoryRes
 */
public interface AccountHistoryService {
    /**
     * 계좌 거래내역 조회.
     *
     * * @param userId 조회 대상 사용자의 ID.
     * 부모가 자녀 계좌를 조회할 때 자녀의 ID를 전달하고,
     * 사용자 본인이 조회할 때는 자신의 ID를 전달합니다.
     * 
     * @param req 거래내역 조회 조건 DTO
     * @param ctx JWT 인증 기반 사용자 컨텍스트(UserContext)
     * @return 거래내역 리스트
     */
    List<AccountHistoryRes> getHistory(Long userId, AccountHistoryReq req, UserContext ctx);

}
