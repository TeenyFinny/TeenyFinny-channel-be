package dev.syntax.domain.investment.dto.res;

import lombok.Builder;

@Builder
public record HoldingItemRes(

        String productCode,
        String productName,

        String quantity,            // 숫자지만 문자열로 포맷
        String avgPrice,            // "13,000"
        String currentPrice,        // "71,800"
        String evaluationAmount,    // "718,000"
        String profitAmount,        // "-20,000"

        String profitRate,          // "12.55"
        Double weight               // "3.25"
) {}
