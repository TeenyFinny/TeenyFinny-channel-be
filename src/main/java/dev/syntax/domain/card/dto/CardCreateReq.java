package dev.syntax.domain.card.dto;

import lombok.Getter;

@Getter
public class CardCreateReq {
    private Long childId;
    private String cardType;      // bear / rabbit
    private String englishName;
    private boolean transit;
    private String password;      // "1234"
}

