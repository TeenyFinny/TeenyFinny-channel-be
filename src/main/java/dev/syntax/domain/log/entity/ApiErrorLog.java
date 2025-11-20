package dev.syntax.domain.log.entity;

import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "api_error_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "error_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private ApiRequestLog requestLog;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "error_detail", columnDefinition = "TEXT")
    private String errorDetail;
}
