package dev.syntax.domain.auth.dto;

/**
 * 푸시 알림 설정 변경 요청 DTO
 * <p>
 * 푸시 알림과 야간 푸시 알림 설정을 변경합니다.
 * 모든 필드는 선택적이며, 제공된 필드만 업데이트됩니다.
 * </p>
 */
public record UpdatePushReq(
	/**
	 * 푸시 알림 활성화 여부 (선택)
	 */
	Boolean pushEnabled,
	
	/**
	 * 야간 푸시 알림 활성화 여부 (선택)
	 */
	Boolean nightPushEnabled
) {
}
