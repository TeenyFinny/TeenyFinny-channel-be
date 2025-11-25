package dev.syntax.domain.auth.dto;

/**
 * 본인인증 응답 DTO
 */
public record IdentityVerifyRes(
        boolean verified,
        String message
) {}
