package dev.syntax.domain.card.service;

import dev.syntax.domain.card.dto.CardCreateReq;
import dev.syntax.domain.card.dto.CardInfoRes;
import dev.syntax.global.auth.dto.UserContext;

/**
 * 카드 생성 서비스 인터페이스.
 */
public interface CardCreateService {

    /**
     * 자녀의 카드를 생성합니다.
     *
     * @param req 카드 생성 요청 정보 (자녀 ID, 카드 타입, 영문명 등)
     * @param ctx 요청한 사용자의 Context (부모 권한 확인용)
     * @return 생성된 카드 정보
     */
    CardInfoRes createCard(CardCreateReq req, UserContext ctx);
}
