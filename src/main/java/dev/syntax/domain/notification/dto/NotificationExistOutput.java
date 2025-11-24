package dev.syntax.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class NotificationExistOutput {
    private final Boolean hasNotice;

    public NotificationExistOutput(Boolean hasNotice) {
        this.hasNotice = hasNotice;
    }
}
