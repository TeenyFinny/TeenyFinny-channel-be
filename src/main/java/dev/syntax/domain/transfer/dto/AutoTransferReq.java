package dev.syntax.domain.transfer.dto;

import java.math.BigDecimal;

import dev.syntax.domain.transfer.enums.AutoTransferType;
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
    @NotNull(message = "자동이체 금액은 필수입니다.")
    @Positive(message = "자동이체 금액은 양수여야 합니다.")
    private BigDecimal totalAmount;

    /**
     * 매월 이체할 날짜 (1~31).
     */
    @NotNull(message = "이체 일자는 필수입니다.")
    @Min(value = 1, message = "이체 일자는 최소 1일이어야 합니다.")
    @Max(value = 31, message = "이체 일자는 최대 31일까지 가능합니다.")
    private Integer transferDate;

    /**
     * 투자 계좌로 이체할 비율 (0~100).
     * <p>
     * - ALLOWANCE일 경우: 0~100 사용<br>
     * - GOAL일 경우: 백엔드에서 자동으로 0으로 강제됨.<br>
     * </p>
     */
    @Min(value = 0, message = "투자 비율은 0 이상이어야 합니다.")
    @Max(value = 100, message = "투자 비율은 100 이하이어야 합니다.")
    private Integer ratio = 0;


    /**
     * 자동이체 목적 타입.
     * <ul>
     *     <li>ALLOWANCE : 용돈/투자 자동이체</li>
     *     <li>GOAL : 목표 자동이체 (투자와 무관, ratio = 0)</li>
     * </ul>
     */
    @NotNull(message = "자동이체 유형은 필수입니다.")
    private AutoTransferType type;
}
