package dev.syntax.domain.transfer.repository;

import dev.syntax.domain.transfer.entity.AutoTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoTransferRepository extends JpaRepository<AutoTransfer, Long> {
}
