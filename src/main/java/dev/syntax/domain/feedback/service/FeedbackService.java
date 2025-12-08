package dev.syntax.domain.feedback.service;

import dev.syntax.domain.feedback.dto.FeedbackCreateReq;
import dev.syntax.domain.feedback.dto.FeedbackRes;
import dev.syntax.global.auth.dto.UserContext;

public interface FeedbackService {
    /**
     * 피드백 생성
     *
     * @param ctx 로그인한 사용자 컨텍스트 (부모 권한)
     * @param req 피드백 생성 요청 정보
     * @return 생성된 피드백 정보
     */
    FeedbackRes createFeedback(UserContext ctx, FeedbackCreateReq req);

    /**
     * 피드백 조회
     *
     * @param ctx      로그인한 사용자 컨텍스트
     * @param reportId 조회할 리포트 ID
     * @return 조회된 피드백 정보
     */
    FeedbackRes getFeedback(UserContext ctx, Long reportId);
    
    /**
     * 피드백 삭제
     *
     * @param feedbackId 삭제할 피드백 ID
     */
    void deleteFeedback(Long feedbackId);
}
