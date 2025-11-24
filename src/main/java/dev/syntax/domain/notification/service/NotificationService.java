package dev.syntax.domain.notification.service;

import dev.syntax.domain.notification.dto.NotificationExistOutput;
import dev.syntax.domain.notification.dto.NotificationOutput;
import dev.syntax.domain.notification.entity.Notification;
import dev.syntax.domain.notification.enums.NotificationType;
import dev.syntax.domain.notification.repository.NotificationRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
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
        Boolean exists = notificationRepository.existsByTargetUserAndIsRead(user, false);
        return new NotificationExistOutput(exists);
    }

    // 2) 알림 리스트 반환 (읽음 처리 X)
    public List<NotificationOutput> findNotice(User user) {
        return notificationRepository.findByTargetUser(user)
                .stream()
                .map(NotificationOutput::new)
                .toList();
    }

    // 3) 알림 읽음 처리
    @Transactional
    public void markAsRead(User user, Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));

        // 본인 알림인지 검증
        if (!notification.getTargetUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorBaseCode.FORBIDDEN);
        }

        notification.markAsRead();
    }

    // 4) 알림 삭제
//    @Transactional
//    public void removeNotice(User user, Long id) {
//
//        Notification notification = notificationRepository.findById(id)
//                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));
//
//        if (!notification.getTargetUser().getId().equals(user.getId())) {
//            throw new BusinessException(ErrorBaseCode.FORBIDDEN);
//        }
//
//        notificationRepository.delete(notification);
//    }
}
