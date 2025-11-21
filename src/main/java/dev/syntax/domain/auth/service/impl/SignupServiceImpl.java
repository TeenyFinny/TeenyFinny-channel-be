package dev.syntax.domain.auth.service.impl;

import org.springframework.stereotype.Service;

import dev.syntax.domain.auth.dto.SignupReq;
import dev.syntax.domain.auth.factory.UserFactory;
import dev.syntax.domain.auth.service.SignupService;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SignupServiceImpl implements SignupService {

	private final UserRepository userRepository;

	@Override
	public void signup(SignupReq inputUser) {

		if (userRepository.findByEmail(inputUser.email()).isPresent()) {
			throw new BusinessException(ErrorAuthCode.EMAIL_CONFLICT);
		}
		User user = UserFactory.create(inputUser);
		userRepository.save(user);
		log.info("회원가입 성공: userId = {}", user.getId());
	}
}
