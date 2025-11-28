package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.PortfolioRes;

public interface InvestPortfolioService {
    PortfolioRes getPortfolio(String cano, int year, int month);
}
