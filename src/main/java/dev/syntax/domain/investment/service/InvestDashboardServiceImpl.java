package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.res.InvestDashboardRes;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestDashboardServiceImpl implements InvestDashboardService {
    private final CoreInvestmentClient coreInvestmentClient;
    private final InvestAccountService accountService;

    @Override
    public InvestDashboardRes getDashboard(UserContext userContext) {
        String cano = accountService.getCanoByUserId(userContext.getId());
        return coreInvestmentClient.getDashboard(cano);
    }
}
