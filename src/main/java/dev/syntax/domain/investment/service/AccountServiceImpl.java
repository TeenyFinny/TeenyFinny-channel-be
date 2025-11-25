package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.investment.dto.res.AccountRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CoreAccountClient coreAccountClient; // Core 호출 클라이언트 주입
    private final UserRepository userRepository;

    @Override
    public AccountRes createInvestmentAccount(Long userId) {
        // 1. Core 서버 호출 (Client 사용)
        var coreResponse = coreAccountClient.createInvestmentAccount(userId);
        if (coreResponse == null || coreResponse.getCano() == null) {
            throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
        }

        String cano = coreResponse.getCano();

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
        return new AccountRes(cano);
    }

    @Override
    public String getCanoByUserId(Long userId) {
        return accountRepository.findByUserIdAndType(userId, AccountType.INVEST)
                .map(Account::getAccountNo)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.TX_ACCOUNT_NOT_FOUND));
    }
}
