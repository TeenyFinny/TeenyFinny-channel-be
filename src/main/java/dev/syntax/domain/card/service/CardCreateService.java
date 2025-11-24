package dev.syntax.domain.card.service;

import dev.syntax.domain.card.dto.CardCreateReq;
import dev.syntax.domain.card.dto.CardInfoRes;
import dev.syntax.global.auth.dto.UserContext;

public interface CardCreateService {
    CardInfoRes createCard(CardCreateReq req, UserContext ctx);
}
