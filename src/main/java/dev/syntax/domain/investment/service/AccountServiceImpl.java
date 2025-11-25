    package dev.syntax.domain.investment.service;

    import dev.syntax.domain.account.entity.Account;
    import dev.syntax.domain.account.enums.AccountType;
    import dev.syntax.domain.account.repository.AccountRepository;

    import java.util.Map;
    import java.util.Optional;

    import dev.syntax.domain.investment.dto.res.AccountRes;
    import dev.syntax.domain.user.entity.User;
    import dev.syntax.domain.user.repository.UserRepository;
    import dev.syntax.global.exception.BusinessException;
    import dev.syntax.global.response.error.ErrorBaseCode;
    import dev.syntax.global.response.error.ErrorCode;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.ResponseEntity;
    import org.springframework.stereotype.Service;
    import org.springframework.web.client.RestTemplate;

    @Service
    @RequiredArgsConstructor
    public class AccountServiceImpl implements AccountService{
        private final AccountRepository accountRepository;

        public String getCanoByUserId(Long userId) {
            Optional<Account> account = accountRepository.findByUserIdAndType(userId, AccountType.INVEST);
            return account.map(Account::getAccountNo)
                    .orElseThrow(() -> new BusinessException(ErrorBaseCode.TX_ACCOUNT_NOT_FOUND));
        }

        private final RestTemplate restTemplate; // 코어 호출용
        private final UserRepository userRepository;

        private static final String CORE_URL = "http://core-server/core/banking/account/investment";

        @Override
        public AccountRes createInvestmentAccount(Long userId) {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    CORE_URL + "?userId=" + userId, null, Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
            }

            String cano = (String) response.getBody().get("cano");
            if (cano == null) {
                throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

            Account account = Account.builder()
                    .user(user)
                    .type(AccountType.INVEST)
                    .accountNo(cano)
                    .build();

            accountRepository.save(account);

            return new AccountRes(cano);
        }

    }
