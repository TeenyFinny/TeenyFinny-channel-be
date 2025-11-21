package dev.syntax.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailValidationReq(
	@NotBlank @Email String email
) {
}
