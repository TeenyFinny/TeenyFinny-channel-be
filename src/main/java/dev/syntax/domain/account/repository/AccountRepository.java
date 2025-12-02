package dev.syntax.domain.account.repository;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * 특정 사용자 + 계좌 타입으로 계좌 조회
     * 예: (userId=5, type=ALLOWANCE)
     */
    Optional<Account> findByUserIdAndType(Long userId, AccountType type);

    /**
     * 계좌번호로 계좌 조회
     * 거래 상세 조회 시 계좌 소유자 검증에 사용
     */
    Optional<Account> findByAccountNo(String accountNo);

    /**
     * 특정 타입의 모든 계좌 조회 (배치 작업용)
     */
    List<Account> findAllByType(AccountType type);
}
