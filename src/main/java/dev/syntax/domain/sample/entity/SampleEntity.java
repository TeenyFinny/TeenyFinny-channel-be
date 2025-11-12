package dev.syntax.domain.sample.entity;

import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 샘플 entity
 */
@Getter
@Setter
@Entity
public class SampleEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "price", nullable = false, updatable = false, columnDefinition = "VARCHAR(64)")
    private String price;
}
