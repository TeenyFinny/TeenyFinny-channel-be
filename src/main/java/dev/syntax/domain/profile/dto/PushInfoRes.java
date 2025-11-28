package dev.syntax.domain.profile.dto;

import lombok.Builder;

@Builder
public record PushInfoRes(
        boolean pushEnabled, // 서비스 알림
        boolean nightPushEnabled // 야간 시간대 알림
) {
}
