package dev.syntax.domain.report.utils;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import dev.syntax.domain.report.enums.Category;

import dev.syntax.domain.report.dto.CoreTransactionRes;

public class ReportUtils {

    /**
     * 거래내역을 카테고리별로 금액 합산
     */
    public static Map<Category, BigDecimal> sumByCategory(List<CoreTransactionRes> histories) {
        Map<Category, BigDecimal> result = new HashMap<>();

        histories.forEach(h ->
                result.merge(h.getCategory(), h.getAmount(), BigDecimal::add)
        );

        return result;
    }
    /**
     * 금액 비율 계산
     */
    public static BigDecimal calcPercent(BigDecimal amount, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return amount
                .divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
