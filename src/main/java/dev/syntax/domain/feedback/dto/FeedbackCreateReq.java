package dev.syntax.domain.feedback.dto;

/**
 * 피드백 생성 요청 DTO
 *
 * @param reportId 피드백을 남길 리포트 ID
 * @param message  작성할 피드백 메시지 내용
 */
public record FeedbackCreateReq(
        Long reportId,
        String message
) {
}