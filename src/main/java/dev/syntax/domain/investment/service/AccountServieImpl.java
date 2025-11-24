package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServieImpl implements AccountService{
    AccountRepository accountRepository;

    public String getCanoByUserId(Long userId) {
        Optional<Account> account = accountRepository.findByUserIdAndType(userId, AccountType.INVEST);
        return account.get().getAccountNo();
    }
}
