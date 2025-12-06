package dev.syntax.domain.feedback.dto;

/**
 * 피드백 조회 응답 DTO
 *
 * @param feedbackId 피드백 ID
 * @param message    피드백 메시지 내용
 */
public record FeedbackRes(
        Long feedbackId,
        String message
) {
}