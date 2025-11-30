package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;

public interface InvestAccountService {
    String getCanoByUserId(Long userId);
    InvestAccountPortfolioRes getInvestAccount(String cano);
    void createInvestmentAccount(Long userId);
}
