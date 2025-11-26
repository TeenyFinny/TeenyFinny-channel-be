package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.res.InvestAccountRes;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvestAccountServiceImpl implements InvestAccountService {
    private final CoreInvestmentClient coreInvestmentClient;
    private final AccountRepository accountRepository;

    @Override
    public String getCanoByUserId(Long userId) {
        Optional<Account> account = accountRepository.findByUserIdAndType(userId, AccountType.INVEST);
        return account.map(Account::getAccountNo)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.TX_ACCOUNT_NOT_FOUND));
    }

    @Override
    public InvestAccountRes getInvestAccount(String cano) {
        return coreInvestmentClient.getInvestAccount(cano);
    }
}
