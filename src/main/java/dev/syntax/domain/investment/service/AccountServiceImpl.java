package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import java.util.Optional;

import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import dev.syntax.global.response.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{
    private final AccountRepository accountRepository;

    public String getCanoByUserId(Long userId) {
        Optional<Account> account = accountRepository.findByUserIdAndType(userId, AccountType.INVEST);
        return account.map(Account::getAccountNo)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.TX_ACCOUNT_NOT_FOUND));
    }
}
