package dev.syntax.domain.notification.dto;

import dev.syntax.domain.notification.entity.Notification;
import dev.syntax.domain.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Getter
@AllArgsConstructor
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
        this.time = formatTime(notification.getCreatedAt());
    }

    private String formatTime(LocalDateTime createdAt) {
        LocalDate today = LocalDate.now();
        LocalDate date = createdAt.toLocalDate();

        long days = ChronoUnit.DAYS.between(date, today);

        if (days == 0) {
            return createdAt.format(DateTimeFormatter.ofPattern("a h:mm").withLocale(Locale.KOREA));
        }
        if (days == 1) {
            return "어제 " + createdAt.format(DateTimeFormatter.ofPattern("a h:mm").withLocale(Locale.KOREA));
        }
        return days + "일 전";
    }
}
