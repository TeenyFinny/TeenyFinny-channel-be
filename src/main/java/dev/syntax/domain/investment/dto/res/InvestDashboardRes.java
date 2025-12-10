package dev.syntax.domain.investment.dto.res;

import dev.syntax.domain.investment.dto.HoldingItem;

import java.util.List;

public record InvestDashboardRes(
        Long userId,
        String depositAmount,
        String totalAssetAmount,
        String totEvluAmt,
        String totalProfitAmount,
        Double totalProfitRate,
        List<HoldingItem> top3Holdings
) {}
