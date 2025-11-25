package dev.syntax.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.auth.dto.SignupReq;
import dev.syntax.domain.auth.factory.UserFactory;
import dev.syntax.domain.user.client.CoreUserClient;
import dev.syntax.domain.user.dto.CoreInitRes;
import dev.syntax.domain.user.dto.CoreUserInitReq;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SignupServiceImpl implements SignupService {

	private final PasswordEncoder encoder;
	private final UserRepository userRepository;
	private final CoreUserClient coreUserClient;

	@Override
	public void signup(SignupReq inputUser) {
		// 1. 이메일 중복 검증
		if (userRepository.existsByEmail(inputUser.email())) {
			throw new BusinessException(ErrorAuthCode.EMAIL_CONFLICT);
		}
		// 2. User 저장
		User user = UserFactory.create(inputUser, encoder);
		userRepository.save(user);
		// 3. CoreUserInitReq 생성
		CoreUserInitReq coreReq = new CoreUserInitReq(
			user.getId(),                   // channelUserId
			user.getRole(),                 // Role(PARENT/CHILD)
			user.getName(),
			user.getPhoneNumber(),
			user.getBirthDate()
		);

		// 4. Core 서버 초기화 호출 (부모/자녀에 따라 응답이 다름)
		CoreInitRes coreRes;
		if (user.getRole() == Role.PARENT) {

			coreRes = coreUserClient.createParentAccount(coreReq);

		} else { // CHILD

			coreRes = coreUserClient.createChildUser(coreReq);
		}
		user.setCoreUserId(coreRes.coreUserId());

		log.info("회원가입 + Core 초기화 완료: channelUserId={}, coreUserId={}",
			user.getId(), user.getCoreUserId());
	}
}
