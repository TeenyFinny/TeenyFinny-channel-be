package dev.syntax.domain.card.repository;

import dev.syntax.domain.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card,Long> {
}
