package dev.syntax.domain.transfer.dto;

import java.math.BigDecimal;

import dev.syntax.global.service.Utils;
import lombok.Builder;
import lombok.Getter;

/**
 * 자동이체 조회 응답 DTO.
 */
@Getter
@Builder
public class AutoTransferRes {

    /**
     * 최초 설정 여부.
     * <p>
     * true: 설정된 자동이체가 없음 (신규)
     * false: 이미 설정된 자동이체가 있음 (수정/삭제 가능)
     * </p>
     */
    private Boolean isInit;

    /**
     * 자동이체 ID.
     * <p>
     * isInit이 false일 때만 존재.
     * </p>
     */
    private Long transferId;

    /**
     * 매월 이체 금액 (콤마 포함 문자열).
     * <p>
     * 예: "25,000"
     * </p>
     */
    private String transferAmount;

    /**
     * 매월 이체 날짜 (1~31).
     */
    private Integer transferDate;

    /**
     * 투자 비율 (0~100).
     */
    private Integer ratio;

    /**
     * 초기화 상태(설정 없음) 응답 생성.
     */
    public static AutoTransferRes init() {
        return AutoTransferRes.builder()
                .isInit(true)
                .build();
    }

    /**
     * 설정 있음 상태 응답 생성.
     */
    public static AutoTransferRes of(Long transferId, BigDecimal amount, Integer date, Integer ratio) {
        return AutoTransferRes.builder()
                .isInit(false)
                .transferId(transferId)
                .transferAmount(Utils.NumberFormattingService(amount))
                .transferDate(date)
                .ratio(ratio)
                .build();
    }
}
