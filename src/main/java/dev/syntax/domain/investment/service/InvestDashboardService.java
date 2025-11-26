package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.InvestDashboardRes;
import dev.syntax.global.auth.dto.UserContext;

public interface InvestDashboardService {
    InvestDashboardRes getDashboard(UserContext userContext);
}