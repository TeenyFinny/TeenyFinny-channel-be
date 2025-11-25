package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.InvestAccountRes;
import dev.syntax.global.auth.dto.UserContext;

public interface AccountService {
    String getCanoByUserId(Long userId);
    InvestAccountRes getInvestAccount(String cano, UserContext userContext);
}
