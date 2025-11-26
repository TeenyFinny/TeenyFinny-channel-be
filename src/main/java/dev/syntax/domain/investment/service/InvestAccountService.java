package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.InvestAccountRes;

public interface InvestAccountService {
    String getCanoByUserId(Long userId);
    InvestAccountRes getInvestAccount(String cano);
}
