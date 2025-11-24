package dev.syntax.domain.card.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CardInfoRes {
    private Long cardId;
    private String cardNumber;
    private String cvc;
    private String expiredAt;
}
