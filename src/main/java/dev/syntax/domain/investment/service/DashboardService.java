package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.DashboardRes;
import dev.syntax.global.auth.dto.UserContext;

public interface DashboardService {
    DashboardRes getDashboard(UserContext userContext);
}
