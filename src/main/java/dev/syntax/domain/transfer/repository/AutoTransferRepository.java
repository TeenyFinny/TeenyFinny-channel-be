package dev.syntax.domain.transfer.repository;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.transfer.entity.AutoTransfer;
import dev.syntax.domain.transfer.enums.AutoTransferType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 자동이체 레포지토리.
 */
public interface AutoTransferRepository extends JpaRepository<AutoTransfer, Long> {
    
    /**
     * 자녀 ID로 자동이체 설정 존재 여부를 확인합니다.
     *
     * @param childId 자녀 ID
     * @return 존재 여부
     */
    boolean existsByUserId(Long childId);


    /**
     * 자녀 ID, 자동이체 목적(용돈/목표)으로 자동이체 설정 존재 여부를 확인합니다.
     * 투자의 경우 용돈에 포함된 걸로 처리합니다.
     *
     * @param childId 자녀 ID
     * @param type 자동이체 목적
     * @return 존재 여부
     */
    boolean existsByUserIdAndType(Long childId, AutoTransferType type);
    
    /**
     * 자녀 ID로 자동이체 설정을 조회합니다.
     *
     * @param childId 자녀 ID
     * @return 자동이체 설정 (Optional)
     */
    Optional<AutoTransfer> findByUserId(Long childId);

    /**
     * 자녀 ID와 타입으로 자동이체 설정을 조회합니다.
     *
     * @param childId 자녀 ID
     * @param type 자동이체 타입
     * @return 자동이체 설정 (Optional)
     */
    Optional<AutoTransfer> findByUserIdAndType(Long childId, AutoTransferType type);


    List<AutoTransfer> findAllByAccountId(Long accountId);


    List<AutoTransfer> account(Account account);

    Optional<AutoTransfer> findByAccountIdAndType(Long accountId, AutoTransferType type);
}
