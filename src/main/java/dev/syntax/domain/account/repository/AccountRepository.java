package dev.syntax.domain.account.repository;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * 특정 사용자 + 계좌 타입으로 계좌 조회
     * 예: (userId=5, type=ALLOWANCE)
     */
    Optional<Account> findByUserIdAndType(Long userId, AccountType type);
}
