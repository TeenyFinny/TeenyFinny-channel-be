package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.dto.res.InvestAccountRes;

public interface InvestAccountService {
    String getCanoByUserId(Long userId);
    InvestAccountPortfolioRes getInvestAccount(String cano);
    InvestAccountRes createInvestmentAccount(Long userId);
    boolean checkAccount(Long userId);
}
