package dev.syntax.domain.notification.dto;

import dev.syntax.domain.notification.entity.Notification;
import dev.syntax.domain.notification.enums.NotificationType;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Data
public class NotificationOutput {

    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private String time;
    private Boolean isRead;

    public NotificationOutput(Notification notification) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.content = notification.getContent();
        this.type = notification.getType();
        this.isRead = notification.getIsRead();
        this.time = formatNotificationTime(notification.getCreatedAt());
        notification.setRead(true);
        this.isRead = notification.getIsRead();
    }

    private String formatNotificationTime(LocalDateTime createdAt) {
        LocalDate today = LocalDate.now();
        LocalDate date = createdAt.toLocalDate();

        long daysBetween = ChronoUnit.DAYS.between(date, today);

        // 오늘
        if (daysBetween == 0) {
            return createdAt.format(DateTimeFormatter.ofPattern("a h:mm")
                    .withLocale(Locale.KOREA));
        }

        // 어제
        if (daysBetween == 1) {
            return "어제 " + createdAt.format(DateTimeFormatter.ofPattern("a h:mm")
                    .withLocale(Locale.KOREA));
        }

        // n일 전
        return daysBetween + "일 전";
    }
}
