package dev.syntax.domain.notification.controller;

import dev.syntax.domain.notification.dto.NotificationExistOutput;
import dev.syntax.domain.notification.dto.NotificationOutput;
import dev.syntax.domain.notification.entity.Notification;
import dev.syntax.domain.notification.service.NotificationService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/notice")
    public NotificationExistOutput noticeCheck(@CurrentUser UserContext userContext) {
        return notificationService.checkNotice(userContext.getUser());
    }

    @GetMapping("/notices")
    public List<NotificationOutput> noticeFind(@CurrentUser UserContext userContext) {
        return notificationService.findNotice(userContext.getUser());
    }

    @DeleteMapping("/notices/{id}")
    public void noticeRemove(@CurrentUser UserContext userContext, @PathVariable Long id) {
        notificationService.removeNotice(id);
    }
}
