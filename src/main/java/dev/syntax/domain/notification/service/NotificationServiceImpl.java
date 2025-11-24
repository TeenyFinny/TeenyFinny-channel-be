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
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // 1) 새 알림 여부 체크
    @Override
    public NotificationExistOutput checkNotice(User user) {
        Boolean exists = notificationRepository.existsByTargetUserAndIsRead(user, false);
        return new NotificationExistOutput(exists);
    }

    // 2) 알림 리스트 반환
    @Override
    public List<NotificationOutput> findNotice(User user) {
        return notificationRepository.findByTargetUser(user)
                .stream()
                .map(NotificationOutput::new)
                .toList();
    }

    // 3) 알림 읽음 처리
    @Override
    @Transactional
    public void markAsRead(User user, Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));

        if (!notification.getTargetUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorBaseCode.FORBIDDEN);
        }

        notification.markAsRead();
    }

    @Override
    @Transactional
    public void sendGoalRequestNotice(User parent, String childName) {
        Notification notification = Notification.builder()
                .targetUser(parent)
                .title("목표 생성 요청")
                .content(childName + "(이)가 목표 생성을 요청했습니다!")
                .type(NotificationType.GOAL)
                .build();

        notificationRepository.save(notification);
    }

    // 4) 알림 삭제
//    @Override
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
