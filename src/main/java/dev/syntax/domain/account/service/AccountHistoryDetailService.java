package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountHistoryDetailRes;
import dev.syntax.global.auth.dto.UserContext;

/**
 * 계좌 거래내역 상세 조회 서비스 인터페이스.
 */
public interface AccountHistoryDetailService {

    /**
     * 단일 거래 상세 조회.
     *
     * @param transactionId 거래 아이디
     * @return 상세 내역 응답 DTO
     */
    AccountHistoryDetailRes getDetail(Long transactionId, UserContext ctx);
}
