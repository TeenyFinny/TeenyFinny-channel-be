package dev.syntax.domain.sample.dto;

import dev.syntax.domain.sample.entity.SampleEntity;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 샘플 DTO
 * @param createdAt
 * @param updatedAt
 * @param price
 */
@Builder
public record SampleDTO (
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String price
) {
    public static SampleDTO create(SampleEntity sample) {
        return SampleDTO.builder()
                .createdAt(sample.getCreatedAt())
                .updatedAt(sample.getUpdatedAt())
                .price(sample.getPrice())
                .build();
    }
}
