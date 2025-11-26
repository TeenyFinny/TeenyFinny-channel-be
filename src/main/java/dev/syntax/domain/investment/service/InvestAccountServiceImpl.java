package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.dto.res.InvestAccountRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    public String getCanoByUserId(Long userId) {
        Optional<Account> account = accountRepository.findByUserIdAndType(userId, AccountType.INVEST);
        return account.map(Account::getAccountNo)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.TX_ACCOUNT_NOT_FOUND));
    }

    @Override
    public InvestAccountPortfolioRes getInvestAccount(String cano) {
        return coreInvestmentClient.getInvestAccount(cano);
    }

    @Override
    public InvestAccountRes createInvestmentAccount(Long userId) {
        // 1. Core 서버 호출 (Client 사용)
        var coreResponse = coreInvestmentClient.createInvestmentAccount(userId);
        if (coreResponse == null || coreResponse.getAccountNumber() == null) {
            throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
        }

        String cano = coreResponse.getAccountNumber();

        // 2. 채널 DB에 계좌 정보 저장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        Account account = Account.builder()
                .user(user)
                .type(AccountType.INVEST)
                .accountNo(cano)
                .build();

        accountRepository.save(account);

        // 3. DTO 반환
        return new InvestAccountRes(cano);
    }

}
