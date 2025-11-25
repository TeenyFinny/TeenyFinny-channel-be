package dev.syntax.domain.investment.dto.res;


import dev.syntax.domain.investment.dto.HoldingItem;

import java.util.List;

public record InvestAccountRes(
        Long userId,
        Long depositAmount,
        Long totEvluAmt,
        Long totalProfitAmount,
        Double totalProfitRate,
        List<HoldingItem> holdings
) {}
