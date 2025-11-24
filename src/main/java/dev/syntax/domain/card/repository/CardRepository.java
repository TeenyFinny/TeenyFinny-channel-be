package dev.syntax.domain.card.repository;

import dev.syntax.domain.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card,Long> {
    /**
     * 특정 계좌(accountId)에 카드가 존재하는지 여부 확인
     */
    boolean existsByAccountId(Long accountId);
}
