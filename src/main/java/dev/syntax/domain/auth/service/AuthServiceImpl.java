package dev.syntax.domain.auth.service;

import org.springframework.stereotype.Service;

import dev.syntax.domain.auth.dto.EmailValidationReq;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;

	@Override
	public void checkEmailDuplicate(EmailValidationReq request) {
		boolean exists = userRepository.existsByEmail(request.email());
		if (exists) {
			throw new BusinessException(ErrorAuthCode.EMAIL_CONFLICT);
		}
	}
}
