package dev.syntax.domain.transfer.repository;

import dev.syntax.domain.transfer.entity.AutoTransfer;

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
     * 자녀 ID로 자동이체 설정을 조회합니다.
     *
     * @param childId 자녀 ID
     * @return 자동이체 설정 (Optional)
     */
    Optional<AutoTransfer> findByUserId(Long childId);

}
