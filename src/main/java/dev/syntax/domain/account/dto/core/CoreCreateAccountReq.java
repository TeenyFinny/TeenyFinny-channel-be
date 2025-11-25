package dev.syntax.domain.account.dto.core;

import lombok.Builder;

/**
 * Core 서버에 계좌 생성 요청을 위한 DTO
 */
@Builder
public record CoreCreateAccountReq(
        Long parentCoreId,
        Long childCoreId
) {
}
