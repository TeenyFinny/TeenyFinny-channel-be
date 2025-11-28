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

/**
 * NotificationServiceImpl
 *
 * <p>알림 생성, 조회, 읽음 처리 등
 * 알림 관련 비즈니스 로직을 수행하는 구현체입니다.</p>
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 새로운 알림 존재 여부 확인
     *
     * @param user 사용자 엔티티
     * @return 읽지 않은 알림 존재 여부
     */
    @Override
    public NotificationExistOutput checkNotice(User user) {
        Boolean exists = notificationRepository.existsByTargetUserAndIsRead(user, false);
        return new NotificationExistOutput(exists);
    }

    /**
     * 사용자 알림 리스트 조회
     *
     * @param user 알림 대상 사용자
     * @return 알림 리스트
     */
    @Override
    public List<NotificationOutput> findNotice(User user) {
        return notificationRepository.findByTargetUser(user)
                .stream()
                .map(NotificationOutput::new)
                .toList();
    }

    /**
     * 알림 읽음 처리
     *
     * @param user 사용자
     * @param id 읽음 처리할 알림 ID
     */
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

    /**
     * 자녀의 목표 생성 요청 알림 생성
     */
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

    /**
     * 자녀의 목표 중도 해지 요청 알림 생성
     */
    @Override
    @Transactional
    public void sendGoalCancelRequestNotice(User parent, String childName, String goalName) {
        String content = childName + "(이)가 '" + goalName + "' 목표 중도 해지를 요청했습니다.";

        if (notificationRepository.existsByTargetUserAndTypeAndContent(parent, NotificationType.GOAL, content)) {
            throw new BusinessException(ErrorBaseCode.GOAL_CANCEL_ALREADY_REQUESTED);
        }

        Notification notification = Notification.builder()
                .targetUser(parent)
                .title("목표 중도 해지 요청")
                .content(content)
                .type(NotificationType.GOAL)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * 자녀의 목표 완료 요청 알림 생성
     */
    @Override
    @Transactional
    public void sendGoalCompleteRequestNotice(User parent, String childName) {
        Notification notification = Notification.builder()
                .targetUser(parent)
                .title("목표 달성 완료")
                .content(childName + "(이)가 목표를 달성했어요!")
                .type(NotificationType.GOAL)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * 알림 삭제 (현재는 주석 처리)
     *
     * <p>필요 시 알림 삭제 기능으로 확장 가능</p>
     */
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
