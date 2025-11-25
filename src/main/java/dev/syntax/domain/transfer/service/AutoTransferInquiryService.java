package dev.syntax.domain.transfer.service;

import dev.syntax.domain.transfer.dto.AutoTransferRes;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.global.auth.dto.UserContext;

/**
 * 자동이체 조회 인터페이스.
 */
public interface AutoTransferInquiryService {
    /**
     * 자동이체 설정 조회.
     *
     * @param childId 자녀 ID
     * @param type 자동이체 타입 (ALLOWANCE / GOAL)
     * @param ctx 사용자 컨텍스트 (권한 확인)
     * @return 자동이체 조회 응답
     */
    AutoTransferRes getAutoTransfer(Long childId, AutoTransferType type, UserContext ctx);
}
