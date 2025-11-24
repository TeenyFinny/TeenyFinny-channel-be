package dev.syntax.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

/**
 * 계좌 요약 정보 응답 DTO.
 * <p>
 * 사용자의 전체 자산 및 각 계좌 유형별 잔액 정보를 포함합니다.
 * 또한 카드 보유 여부 정보도 함께 제공합니다.
 * </p>
 */
@Getter
@AllArgsConstructor
public class AccountSummaryRes {

    /**
     * 총 자산 금액 (모든 계좌 잔액의 합).
     */
    private String total;

    /**
     * 용돈 계좌(ALLOWANCE) 잔액.
     */
    private String allowance;

    /**
     * 투자 계좌(INVEST) 잔액.
     */
    private String invest;

    /**
     * 저축 계좌(SAVING) 잔액.
     */
    private String saving;

    /**
     * 카드 정보 (보유 여부 등).
     */
    private CardInfo card;

    /**
     * 카드 관련 정보 DTO.
     */
    @Getter
    @AllArgsConstructor
    public static class CardInfo {
        /**
         * 카드 보유 여부.
         * true: 카드 보유 중, false: 미보유
         */
        private boolean hasCard;
    }
}
