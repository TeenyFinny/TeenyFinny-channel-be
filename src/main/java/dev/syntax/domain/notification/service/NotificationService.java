package dev.syntax.domain.notification.service;

import java.util.List;

import dev.syntax.domain.notification.dto.NotificationExistOutput;
import dev.syntax.domain.notification.dto.NotificationOutput;
import dev.syntax.domain.user.entity.User;

/**
 * NotificationService
 *
 * <p>알림 관련 기능을 제공하는 서비스 인터페이스입니다.<br>
 * 알림 조회, 읽음 처리, 특정 이벤트에 대한 알림 생성 등을 정의합니다.</p>
 */
public interface NotificationService {

    /**
     * 새로운 알림이 존재하는지 확인
     *
     * @param user 사용자 엔티티
     * @return 읽지 않은 알림 여부
     */
    NotificationExistOutput checkNotice(User user);

    /**
     * 사용자 알림 리스트 조회
     *
     * @param user 알림 대상 사용자
     * @return 알림 DTO 리스트
     */
    List<NotificationOutput> findNotice(User user);

    /**
     * 특정 알림 읽음 처리
     *
     * @param user 알림 대상 사용자
     * @param id 알림 ID
     */
    void markAsRead(User user, Long id);

//    /**
//     * 알림 삭제
//     *
//     * @param user 알림 대상 사용자
//     * @param id 삭제할 알림 ID
//     */
//    void removeNotice(User user, Long id);

    /**
     * 자녀의 목표 생성 요청 알림 생성
     *
     * @param parent 부모 사용자
     * @param childName 자녀 이름
     */
    void sendGoalRequestNotice(User parent, String childName);

    /**
     * 자녀의 목표 중도 해지 요청 알림 생성
     *
     * @param parent 부모 사용자
     * @param childName 자녀 이름
     * @param goalName 목표 이름
     */
    void sendGoalCancelRequestNotice(User parent, String childName, String goalName);

    /**
     * 자녀의 목표 완료 요청 알림 생성
     *
     * @param parent 부모 사용자
     * @param childName 자녀 이름
     */
    void sendGoalCompleteRequestNotice(User parent, String childName);

	/**
	 * 가족 등록 완료 요청 알림 생성 (부모용)
	 *
	 * @param parent 부모 사용자
	 * @param childName 자녀 이름
	 */
	void sendFamilyRegistrationNotice(User parent, String childName);

	/**
	 * 가족 등록 완료 요청 알림 생성 (자녀용)
	 *
	 * @param child 자녀 사용자
	 * @param parentName 부모 이름
	 */
	void sendFamilyRegistrationChildNotice(User child, String parentName);

	void sendGoalAchievedNotice(User child);

	void sendGoalAccountCreatedNotice(User child);

	void sendGoalCancelConfirm(User child);
}
