package dev.syntax.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.notification.dto.NotificationExistOutput;
import dev.syntax.domain.notification.dto.NotificationOutput;
import dev.syntax.domain.notification.entity.Notification;
import dev.syntax.domain.notification.enums.NotificationType;
import dev.syntax.domain.notification.repository.NotificationRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;

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
    private final SseService sseService;
	private static final String NOTIFICATION = "notification";

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
        return notificationRepository.findByTargetUserOrderByIdDesc(user)
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
        sseService.send(parent.getId(), NOTIFICATION, new NotificationOutput(notification));
    }

    /**
     * 자녀의 목표 중도 해지 요청 알림 생성
     */
    @Override
    @Transactional
    public void sendGoalCancelRequestNotice(User parent, String childName, String goalName) {
        String title = "목표 중도 해지 요청";
        String content = String.format("%s(이)가 '%s' 목표 중도 해지를 요청했습니다.", childName, goalName);

        if (notificationRepository.existsByTargetUserAndTypeAndContent(parent, NotificationType.GOAL, content)) {
            throw new BusinessException(ErrorBaseCode.GOAL_CANCEL_ALREADY_REQUESTED);
        }

        Notification notification = Notification.builder()
                .targetUser(parent)
                .title(title)
                .content(content)
                .type(NotificationType.GOAL)
                .build();

        notificationRepository.save(notification);
        sseService.send(parent.getId(), NOTIFICATION, new NotificationOutput(notification));
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
        sseService.send(parent.getId(), NOTIFICATION, new NotificationOutput(notification));
    }

    /**
     * 가족 등록 완료 알림 생성 (부모용)
     */
	@Override
	@Transactional
	public void sendFamilyRegistrationNotice(User parent, String childName) {
		createAndSendNotification(parent, "가족 등록 완료", childName + "님과 가족 연결이 완료되었습니다!", NotificationType.SYSTEM);
	}

    /**
     * 가족 등록 완료 알림 생성 (자녀용)
     */
	@Override
	@Transactional
	public void sendFamilyRegistrationChildNotice(User child, String parentName) {
		createAndSendNotification(child, "가족 등록 완료", parentName + "님과 가족 연결이 완료되었습니다!", NotificationType.SYSTEM);
	}

	private void createAndSendNotification(User targetUser, String title, String content, NotificationType type) {
		Notification notification = Notification.builder()
			.targetUser(targetUser)
			.title(title)
			.content(content)
			.type(type)
			.build();

		notificationRepository.save(notification);
		sseService.send(targetUser.getId(), NOTIFICATION, new NotificationOutput(notification));
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
