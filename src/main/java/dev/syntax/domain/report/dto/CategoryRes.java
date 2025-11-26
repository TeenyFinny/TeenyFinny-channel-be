package dev.syntax.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 카테고리별 지출 내역 응답 DTO.
 */
@Getter
@AllArgsConstructor
public class CategoryRes {
    /** 카테고리명 (예: "이체", "식비") */
    private String category;

    /** 지출 금액 (콤마 포함 문자열, 예: "328,830") */
    private String amount;

    /** 지출 비중 (퍼센트, 예: 59.3) */
    private double percentage;
}
