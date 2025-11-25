package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordVerifyReq(
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
                message = "비밀번호는 8자리 이상이어야 하며, 특수문자를 포함해야 합니다."
        )
        String password

) { }
