package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.DashboardRes;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService{
    private final CoreInvestmentClient coreInvestmentClient;
    AccountService accountService;

    @Override
    public DashboardRes getDashboard(UserContext userContext) {
        String cano = accountService.getCanoByUserId(userContext.getId());
        return coreInvestmentClient.getDasgboard(cano);
    }
}
