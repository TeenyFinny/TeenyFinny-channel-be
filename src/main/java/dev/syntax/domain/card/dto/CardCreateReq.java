package dev.syntax.domain.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 카드 생성 요청 DTO.
 */
@Getter
public class CardCreateReq {

    /**
     * 카드를 발급할 자녀의 ID.
     */
    @NotNull(message = "자녀 ID는 필수입니다.")
    private Long childId;

    /**
     * 카드 디자인 타입 (예: bear, rabbit).
     */
    @NotBlank(message = "카드 타입은 필수입니다.")
    private String cardType;

    /**
     * 카드에 각인할 영문 이름.
     */
    @NotBlank(message = "영문 이름은 필수입니다.")
    @Pattern(regexp = "^[a-zA-Z ]*$", message = "영문 이름은 영문과 공백만 입력 가능합니다.")
    private String englishName;

    /**
     * 후불 교통카드 기능 신청 여부.
     */
    private boolean transit;

    /**
     * 카드 비밀번호 (숫자 4자리).
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, max = 4, message = "비밀번호는 6자리여야 합니다.")
    @Pattern(regexp = "\\d{4}", message = "비밀번호는 숫자로만 구성되어야 합니다.")
    private String password;
}

