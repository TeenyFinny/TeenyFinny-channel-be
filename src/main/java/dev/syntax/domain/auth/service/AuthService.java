package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.EmailValidationReq;

public interface AuthService {
	void checkEmailDuplicate(EmailValidationReq request);
}

