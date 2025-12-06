package dev.syntax.domain.report.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 월간 금융 리포트 응답 DTO.
 */
@Getter
@AllArgsConstructor
public class ReportRes {
    private Long reportId;

    /** 리포트 해당 월 (1~12) */
    private int month;

    /** 총 지출 금액 (콤마 포함 문자열, 예: "554,957") */
    private String totalAmount;

    /** 전월 대비 차액 (콤마 포함 문자열, 예: "30,000") */
    private String comparedAmount;

    /** 전월 대비 지출 유형 ("more": 더 씀, "less": 덜 씀, "same": 같음) */
    private String comparedType;

    /** 카테고리별 지출 내역 리스트 */
    private List<CategoryRes> categories;
}