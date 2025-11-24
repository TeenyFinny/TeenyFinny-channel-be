package dev.syntax.domain.notification.service;

import dev.syntax.domain.notification.dto.NotificationExistOutput;
import dev.syntax.domain.notification.dto.NotificationOutput;
import dev.syntax.domain.notification.entity.Notification;
import dev.syntax.domain.notification.enums.NotificationType;
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

    // 1) 새 알림 여부 체크
    public NotificationExistOutput checkNotice(User user) {
        Boolean exists = notificationRepository.existByTargetUserAndIsRead(user, false);
        return new NotificationExistOutput(exists);
    }

    // 2) 알림 리스트 반환 (읽음 처리 절대 X)
    public List<NotificationOutput> findNotice(User user) {
        return notificationRepository.findByTargetUser(user)
                .stream()
                .map(NotificationOutput::new)
                .toList();
    }

    // 3) 알림 읽음 처리 (사용자가 클릭했을 때)
    @Transactional
    public void markAsRead(User user, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("알림 없음"));

        if (!notification.getTargetUser().equals(user)) {
            throw new RuntimeException("내 알림이 아님");
        }

        notification.markAsRead();
    }

    // 4) 샘플 알림 생성
    @Transactional
    public void createSampleNotice(User user) {
        Notification notification = Notification.builder()
                .targetUser(user)
                .title("🎉 테스트 알림")
                .content("이것은 테스트용 알림입니다.")
                .type(NotificationType.SYSTEM)
                .build();

        notificationRepository.save(notification);
    }
}
