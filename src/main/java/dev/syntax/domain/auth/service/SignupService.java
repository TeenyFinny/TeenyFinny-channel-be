package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.SignupReq;

public interface SignupService {
	void signup(SignupReq inputUser);
}
