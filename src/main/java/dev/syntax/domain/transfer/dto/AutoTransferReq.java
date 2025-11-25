package dev.syntax.domain.transfer.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 자동이체 설정 요청 DTO.
 */
@Getter
@NoArgsConstructor
public class AutoTransferReq {

    /**
     * 매월 이체할 총 금액 (예: 100000).
     */
    @NotNull
    @Positive
    private BigDecimal totalAmount;

    /**
     * 매월 이체할 날짜 (1~31).
     */
    @NotNull
    @Min(1)
    @Max(31)
    private Integer transferDate;

    /**
     * 투자 계좌로 이체할 비율 (0~100).
     * <p>
     * 나머지(100 - ratio)는 용돈 계좌로 이체됩니다.
     * </p>
     */
    @NotNull
    @Min(0)
    @Max(100)
    private Integer ratio;
}
