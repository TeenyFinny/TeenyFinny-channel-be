package dev.syntax.domain.feedback.service;

import dev.syntax.domain.feedback.dto.FeedbackCreateReq;
import dev.syntax.domain.feedback.dto.FeedbackRes;
import dev.syntax.domain.feedback.entity.Feedback;
import dev.syntax.domain.feedback.repository.FeedbackRepository;
import dev.syntax.domain.notification.service.NotificationService;
import dev.syntax.domain.report.entity.SummaryReport;
import dev.syntax.domain.report.repository.SummaryReportRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SummaryReportRepository summaryReportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    /**
     * 부모 피드백 생성
     * <p>
     * 1. 부모 권한 확인<br>
     * 2. 리포트 존재 여부 확인<br>
     * 3. 부모-자녀 관계 확인<br>
     * 4. 이미 작성된 피드백이 있는지 확인 (중복 방지)<br>
     * 5. 피드백 저장 및 자녀에게 알림 전송
     * </p>
     *
     * @param ctx 로그인한 사용자 컨텍스트 (부모)
     * @param req 피드백 생성 요청 (리포트 ID, 메시지)
     * @return 생성된 피드백 정보
     */
    @Override
    public FeedbackRes createFeedback(UserContext ctx, FeedbackCreateReq req) {
        log.info("[피드백 생성 시도] parentId={}, reportId={}", ctx.getId(), req.reportId());

        // 부모 권한 체크
        if (!ctx.getRole().equals(Role.PARENT.name())) {
            log.warn("[피드백 생성 실패] 권한 없음 - 사용자 역할: {}", ctx.getRole());
            throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
        }

        SummaryReport report = summaryReportRepository.findById(req.reportId())
                .orElseThrow(() -> {
                    log.error("[피드백 생성 실패] 리포트 없음 - reportId={}", req.reportId());
                    return new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY);
                });

        Long childId = report.getUser().getId();

        // 부모 → 자녀 관계 체크
        if (!ctx.getChildren().contains(childId)) {
            log.warn("[피드백 생성 실패] 부모-자녀 관계 불일치 - parentId={}, childId={}", ctx.getId(), childId);
            throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
        }

        // 중복 체크
        if (feedbackRepository.existsByReport(report)) {
            log.warn("[피드백 생성 실패] 이미 피드백 존재 - reportId={}", req.reportId());
            throw new BusinessException(ErrorBaseCode.FEEDBACK_ALREADY_EXISTS);
        }

        Feedback feedback = Feedback.builder()
                .report(report)
                .writer(ctx.getUser())
                .message(req.message())
                .build();

        feedbackRepository.save(feedback);
        log.debug("[피드백 저장 완료] feedbackId={}", feedback.getId());

        User child = report.getUser();
        try {
            notificationService.sendFeedbackNotice(child, report.getYear(), report.getMonth());
            log.info("[피드백 알림 전송 완료] childId={}, year={}, month={}", child.getId(), report.getYear(), report.getMonth());
        } catch (Exception e) {
            log.error("[피드백 알림 전송 실패] childId={}", child.getId(), e);
            // 알림 실패가 피드백 생성 실패로 이어지지는 않도록 예외 처리
        }

        log.info("[피드백 생성 완료] feedbackId={}, parentId={}, childId={}", feedback.getId(), ctx.getId(), childId);

        return new FeedbackRes(feedback.getId(), feedback.getMessage());
    }

    /**
     * 피드백 조회
     * <p>
     * 리포트에 달린 피드백을 조회합니다.
     * 자녀(리포트 주인) 또는 부모(연결된 자녀)만 조회 가능합니다.
     * </p>
     *
     * @param ctx      로그인한 사용자 컨텍스트
     * @param reportId 조회할 리포트 ID
     * @return 피드백 정보 (없으면 null 반환)
     */
    @Override
    @Transactional(readOnly = true)
    public FeedbackRes getFeedback(UserContext ctx, Long reportId) {
        log.debug("[피드백 조회 시도] userId={}, reportId={}", ctx.getId(), reportId);

        SummaryReport report = summaryReportRepository.findById(reportId)
                .orElseThrow(() -> {
                    log.error("[피드백 조회 실패] 리포트 없음 - reportId={}", reportId);
                    return new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY);
                });

        Long ownerId = report.getUser().getId();  // 리포트 주인 (자녀)

        boolean isChildOwner =
                ctx.getRole().equals(Role.CHILD.name()) &&
                ctx.getId().equals(ownerId);

        boolean isParentOfOwner =
                ctx.getRole().equals(Role.PARENT.name()) &&
                ctx.getChildren().contains(ownerId);

        // 부모 or 자녀 중 누구라도 조건 충족해야 조회 가능
        if (!(isChildOwner || isParentOfOwner)) {
            log.warn("[피드백 조회 실패] 권한 없음 - userId={}, reportOwnerId={}", ctx.getId(), ownerId);
            throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
        }

        Feedback feedback = feedbackRepository.findByReport(report).orElse(null);

        if (feedback == null) {
            log.debug("[피드백 조회] 피드백 없음 - reportId={}", reportId);
            return new FeedbackRes(null, null);
        }

        log.info("[피드백 조회 성공] feedbackId={}", feedback.getId());
        return new FeedbackRes(feedback.getId(), feedback.getMessage());
    }

    /**
     * 피드백 삭제
     *
     * @param feedbackId 삭제할 피드백 ID
     */
    @Override
    @Transactional
    public void deleteFeedback(Long feedbackId) {
        log.info("[피드백 삭제 시도] feedbackId={}", feedbackId);
        try {
            feedbackRepository.deleteById(feedbackId);
            log.info("[피드백 삭제 성공] feedbackId={}", feedbackId);
        } catch (Exception e) {
            log.error("[피드백 삭제 실패] feedbackId={}", feedbackId, e);
            throw e;
        }
    }
}
