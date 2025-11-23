package dev.syntax.domain.notification.service;

import dev.syntax.domain.notification.dto.NotificationExistOutput;
import dev.syntax.domain.notification.dto.NotificationOutput;
import dev.syntax.domain.notification.repository.NotificationRepository;
import dev.syntax.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationExistOutput checkNotice(User user) {
        return new NotificationExistOutput(notificationRepository.existByTargetUserAndIsRead(user, false));
    }

    public List<NotificationOutput> findNotice(User user) {
        return notificationRepository.findByTargetUser(user).stream().map(NotificationOutput::new).toList();
    }

    @Transactional
    public void removeNotice(Long id) {
        notificationRepository.deleteById(id);
    }
}
