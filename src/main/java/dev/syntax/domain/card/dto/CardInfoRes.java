package dev.syntax.domain.card.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 카드 발급 정보 응답 DTO.
 */
@Getter
@AllArgsConstructor
public class CardInfoRes {

    /**
     * 생성된 카드의 고유 ID (PK).
     */
    private Long cardId;

    /**
     * 카드 번호 (16자리). xxxx xxxx xxxx xxxx
     */
    private String cardNumber;

    /**
     * 카드 이름(별명) - 초기에는 영문 이름 저장
     */
    private String name;

    /**
     * CVC 번호 (3자리).
     */
    private String cvc;

    /**
     * 유효기간 (MM/yy).
     */
    private String expiredAt;
}
