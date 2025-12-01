package dev.syntax.domain.card.repository;

import dev.syntax.domain.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Card 엔티티에 대한 데이터 접근을 담당하는 Repository.
 */
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * 특정 계좌(accountId)에 연결된 카드가 존재하는지 확인합니다.
     *
     * @param accountId 확인할 계좌 ID
     * @return 카드 존재 여부 (true: 존재함, false: 없음)
     */
    boolean existsByAccountId(Long accountId);

    /**
     * 특정 카드 번호(number)가 이미 존재하는지 확인합니다.
     * (카드 번호 중복 생성 방지용)
     *
     * @param number 확인할 카드 번호
     * @return 카드 번호 존재 여부 (true: 이미 존재함, false: 사용 가능)
     */
    boolean existsByNumber(String number);

    /**
     * 계좌 ID로 카드를 조회합니다.
     *
     * @param accountId 계좌 ID
     * @return 카드 Optional
     */
    Optional<Card> findByAccountId(Long accountId);
}
