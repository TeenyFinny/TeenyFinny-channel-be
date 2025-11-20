package dev.syntax.domain.log.entity;

import dev.syntax.domain.log.enums.Category;
import dev.syntax.domain.user.entity.User;
import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "api_request_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiRequestLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private Category category;

    @Column(name = "request_content", nullable = false, columnDefinition = "TEXT")
    private String requestContent;

    @Column(name = "response_code")
    private Integer responseCode;   // 기본 null

    @Builder.Default
    @Column(name = "success", nullable = false)
    private Boolean success = false;
}
