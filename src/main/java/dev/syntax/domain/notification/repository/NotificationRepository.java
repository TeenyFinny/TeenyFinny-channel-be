package dev.syntax.domain.notification.repository;

import dev.syntax.domain.notification.entity.Notification;
import dev.syntax.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Transactional(readOnly = true)
    Boolean existsByTargetUserAndIsRead(User user, Boolean isRead);

    List<Notification> findByTargetUser(User targetUser);

    boolean existsByTargetUserAndTypeAndContent(User targetUser, dev.syntax.domain.notification.enums.NotificationType type, String content);
}
