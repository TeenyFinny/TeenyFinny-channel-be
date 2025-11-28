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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public InvestAccountRes createInvestmentAccount(Long userId) {
        // 채널 DB에 투자 계좌가 이미 존재하는지 확인
        accountRepository.findByUserIdAndType(userId, AccountType.INVEST).ifPresent(account -> {
            throw new BusinessException(ErrorBaseCode.CONFLICT);
        });

        // Core 서버 호출 (Client 사용)
        var coreResponse = coreInvestmentClient.createInvestmentAccount(userId);
        if (coreResponse == null || coreResponse.getAccountNumber() == null) {
            throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
        }

        String cano = coreResponse.getAccountNumber();

        // 채널 DB에 계좌 정보 저장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        Account account = Account.builder()
                .user(user)
                .type(AccountType.INVEST)
                .accountNo(cano)
                .build();

        accountRepository.save(account);

        // DTO 반환
        return new InvestAccountRes(cano);
    }

    @Override
    public boolean checkAccount(Long userId) {
        // 1. 채널 DB 확인
        boolean existsInChannel = accountRepository.findByUserIdAndType(userId, AccountType.INVEST).isPresent();
        if (existsInChannel) return true;

        // 2. Core Banking Mock 확인 (데이터 불일치 가능성 대비)
        return coreInvestmentClient.checkAccount(userId);
    }

}
