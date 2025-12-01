package dev.syntax.domain.notification.controller;

import dev.syntax.domain.notification.dto.NotificationExistOutput;
import dev.syntax.domain.notification.dto.NotificationOutput;
import dev.syntax.domain.notification.service.NotificationService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NotificationController
 *
 * <p>알림(Notification) 기능을 제공하는 API 컨트롤러입니다.<br>
 * 알림 존재 여부 확인, 알림 목록 조회, 읽음 처리 등의 기능을 제공합니다.</p>
 */
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 새로운 알림 여부 확인
     *
     * <p>사용자의 읽지 않은 알림이 존재하는지 확인합니다.</p>
     *
     * @param userContext 현재 로그인한 사용자 정보
     * @return 새로운 알림 존재 여부를 담은 응답
     */
    @GetMapping("/notice")
    public ResponseEntity<BaseResponse<?>> noticeCheck(@CurrentUser UserContext userContext) {
        NotificationExistOutput result =
                notificationService.checkNotice(userContext.getUser());

        return ApiResponseUtil.success(SuccessCode.OK, result);
    }

    /**
     * 알림 목록 조회 (읽음 처리 X)
     *
     * <p>사용자의 알림 내역을 모두 조회합니다.<br>
     * 각 알림의 읽음 여부는 유지되며 조회만 수행됩니다.</p>
     *
     * @param userContext 현재 로그인한 사용자 정보
     * @return 알림 리스트 응답
     */
    @GetMapping("/notices")
    public ResponseEntity<BaseResponse<?>> noticeFind(@CurrentUser UserContext userContext) {
        List<NotificationOutput> result =
                notificationService.findNotice(userContext.getUser());

        return ApiResponseUtil.success(SuccessCode.OK, result);
    }

    /**
     * 알림 읽음 처리
     *
     * <p>해당 알림을 클릭(열람)하여 읽음 상태로 변경합니다.</p>
     *
     * @param userContext 현재 로그인한 사용자 정보
     * @param id 읽음 처리할 알림 ID
     * @return 성공 응답 (body 없음)
     */
    @PatchMapping("/notices/{id}/read")
    public ResponseEntity<BaseResponse<?>> markAsRead(
            @CurrentUser UserContext userContext,
            @PathVariable Long id
    ) {
        notificationService.markAsRead(userContext.getUser(), id);
        return ApiResponseUtil.success(SuccessCode.OK);
    }

    /**
     * 알림 삭제
     *
     * <p>현재는 사용하지 않지만, 향후 필요 시 사용할 수 있도록 주석 처리된 API입니다.</p>
     *
     * @param id 삭제할 알림 ID
     */
//    @DeleteMapping("/notices/{id}")
//    public ResponseEntity<BaseResponse<?>> noticeRemove(@PathVariable Long id) {
//        notificationService.removeNotice(id);
//        return ApiResponseUtil.success(SuccessCode.OK);
//    }
}
