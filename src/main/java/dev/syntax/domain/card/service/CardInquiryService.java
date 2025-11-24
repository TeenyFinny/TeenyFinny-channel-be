package dev.syntax.domain.card.service;

import dev.syntax.domain.card.dto.CardInfoRes;
import dev.syntax.global.auth.dto.UserContext;

public interface CardInquiryService {

    /**
     * 카드 정보 조회 (자녀 또는 부모)
     *
     * @param targetUserId 조회 대상 사용자(자녀)의 ID
     * @param ctx 현재 로그인한 사용자 정보
     * @return 카드 정보 응답 DTO
     */
    CardInfoRes getCardInfo(Long targetUserId, UserContext ctx);
}
