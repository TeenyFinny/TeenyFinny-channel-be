package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.AccountRes;

public interface AccountService {
    String getCanoByUserId(Long userId);
    AccountRes createInvestmentAccount(Long userId);
}
