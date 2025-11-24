package dev.syntax.domain.notification.controller;

import dev.syntax.domain.notification.dto.NotificationExistOutput;
import dev.syntax.domain.notification.dto.NotificationOutput;
import dev.syntax.domain.notification.entity.Notification;
import dev.syntax.domain.notification.service.NotificationService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 1) 새로운 알림 있는지
    @GetMapping("/notice")
    public NotificationExistOutput noticeCheck(@CurrentUser UserContext userContext) {
        return notificationService.checkNotice(userContext.getUser());
    }

    // 2) 알림 리스트 불러오기 (읽음 처리 X)
    @GetMapping("/notices")
    public List<NotificationOutput> noticeFind(@CurrentUser UserContext userContext) {
        return notificationService.findNotice(userContext.getUser());
    }

    // 3) 알림 클릭 시 읽음 처리
    @PatchMapping("/notices/{id}/read")
    public void markAsRead(@CurrentUser UserContext userContext, @PathVariable Long id) {
        notificationService.markAsRead(userContext.getUser(), id);
    }

//    // 4) 알림 삭제
//    @DeleteMapping("/notices/{id}")
//    public void noticeRemove(@PathVariable Long id) {
//        notificationService.removeNotice(id);
//    }
}
