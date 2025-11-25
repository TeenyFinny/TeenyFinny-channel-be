package dev.syntax.domain.transfer.service;

import dev.syntax.domain.transfer.dto.AutoTransferReq;
import dev.syntax.global.auth.dto.UserContext;

/**
 * 자동이체 생성 인터페이스.
 */
public interface AutoTransferCreateService {

    /**
     * 자동이체 설정 생성 (부모 -> 자녀).
     *
     * @param childId 자녀 ID
     * @param req 자동이체 설정 요청 (금액, 날짜, 비율)
     * @param ctx 사용자 컨텍스트 (부모 권한 확인)
     */
    void createAutoTransfer(Long childId, AutoTransferReq req, UserContext ctx);

}
