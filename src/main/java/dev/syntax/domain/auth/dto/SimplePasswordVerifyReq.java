package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SimplePasswordVerifyReq(
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "숫자 6자리여야 합니다.")
        String password
) { }
