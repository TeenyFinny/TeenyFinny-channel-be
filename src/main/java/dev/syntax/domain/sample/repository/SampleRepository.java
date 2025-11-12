package dev.syntax.domain.sample.repository;

import dev.syntax.domain.sample.entity.SampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 샘플 repository
 */
@Repository
public interface SampleRepository extends JpaRepository<SampleEntity, Long> {
}
