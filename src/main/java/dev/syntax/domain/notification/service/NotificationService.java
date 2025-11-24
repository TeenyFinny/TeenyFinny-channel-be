package dev.syntax.domain.notification.service;

import dev.syntax.domain.notification.dto.NotificationExistOutput;
import dev.syntax.domain.notification.dto.NotificationOutput;
import dev.syntax.domain.user.entity.User;

import java.util.List;

public interface NotificationService {

    NotificationExistOutput checkNotice(User user);

    List<NotificationOutput> findNotice(User user);

    void markAsRead(User user, Long id);

//    void removeNotice(User user, Long id);

    void sendGoalRequestNotice(User parent, String childName);

    void sendGoalCancelRequestNotice(User parent, String childName);

    void sendGoalCompleteRequestNotice(User parent, String childName);
}
