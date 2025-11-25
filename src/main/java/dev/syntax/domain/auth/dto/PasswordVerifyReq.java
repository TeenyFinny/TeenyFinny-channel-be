package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordVerifyReq(
        @NotBlank String password
) { }
