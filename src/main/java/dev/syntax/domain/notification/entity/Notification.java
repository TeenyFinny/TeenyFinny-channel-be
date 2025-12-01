package dev.syntax.domain.notification.entity;

import dev.syntax.domain.notification.enums.NotificationType;
import dev.syntax.domain.user.entity.User;
import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Notification
 *
 * <p>사용자에게 전달되는 알림(Notification)을 저장하는 엔티티입니다.<br>
 * 목표 생성 요청, 목표 중도해지 요청, 목표 완료 알림 등
 * 다양한 타입의 알림 정보를 포함합니다.</p>
 */
@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    /**
     * 알림 고유 ID (PK)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    /**
     * 알림을 전달받는 대상 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User targetUser;

    /**
     * 알림 제목
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 알림 내용
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 알림 유형 (GOAL 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    /**
     * 읽음 여부
     */
    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * 알림을 읽음 상태로 변경
     */
    public void markAsRead() {
        this.isRead = true;
    }
}
