package dev.syntax.domain.notification.service;

import dev.syntax.domain.notification.entity.Notification;
import dev.syntax.domain.notification.repository.NotificationRepository;
import dev.syntax.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {
    private final NotificationRepository notificationRepository;

    private void sendNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    private void sendEachNotification(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }
}
