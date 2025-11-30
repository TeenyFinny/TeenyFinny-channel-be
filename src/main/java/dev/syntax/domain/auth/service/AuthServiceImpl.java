package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.*;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.auth.jwt.JwtTokenProvider;
import dev.syntax.global.auth.validator.IdentityValidator;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void checkEmailDuplicate(EmailValidationReq request) {
		boolean exists = userRepository.existsByEmail(request.email());
		if (exists) {
			throw new BusinessException(ErrorAuthCode.EMAIL_CONFLICT);
		}
	}

	@Override
	public RefreshTokenRes refreshToken(Long userId) {
		// 최신 User 정보 조회 (가족 관계 포함)
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorAuthCode.UNAUTHORIZED));

		// 최신 정보로 UserContext 생성
		UserContext userContext = new UserContext(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userContext,
			null,
			userContext.getAuthorities()
		);

		// 새 JWT 토큰 발급
		String newAccessToken = jwtTokenProvider.generateToken(authentication);

		return RefreshTokenRes.builder()
			.accessToken(newAccessToken)
			.build();
	}

	@Override
	public PasswordVerifyRes verifyPassword(Long userId, PasswordVerifyReq request) {

		// 1. 사용자 조회
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorAuthCode.UNAUTHORIZED));

		// 2. 비밀번호 비교 (PasswordEncoder 사용)
		boolean matched = passwordEncoder.matches(request.password(), user.getPassword());

		if (!matched) {
			throw new BusinessException(ErrorAuthCode.PASSWORD_MISMATCH);
			// == "AUTH04", "패스워드를 확인해주세요."
		}

		// 3. 성공 시 반환
		return new PasswordVerifyRes(true);
	}

	@Override
	public PasswordVerifyRes verifySimplePassword(Long userId, SimplePasswordVerifyReq request) {

		// 사용자 조회
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorAuthCode.UNAUTHORIZED));

		// 간편비밀번호 비교 (DB에 저장된 간편비밀번호가 있다고 가정)
		boolean matched = passwordEncoder.matches(request.password(), user.getSimplePassword());

		if (!matched) {
			throw new BusinessException(ErrorAuthCode.SIMPLE_PASSWORD_MISMATCH); // 새 에러코드
		}

		return new PasswordVerifyRes(true);
	}

	@Override
	@Transactional
	public void updatePassword(UserContext userContext, UpdatePasswordReq request) {
		// DB에서 User 엔티티를 다시 조회 (영속성 컨텍스트에서 관리되는 엔티티)
		User user = userRepository.findById(userContext.getId())
			.orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

		// 현재 비밀번호 검증
		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			log.warn("[비밀번호 변경 실패] userId: {}, reason: 현재 비밀번호 불일치", user.getId());
			throw new BusinessException(ErrorAuthCode.PASSWORD_MISMATCH);
		}

		// 새 비밀번호 암호화 및 업데이트
		String encodedNewPassword = passwordEncoder.encode(request.newPassword());
		user.updatePassword(encodedNewPassword);

		log.info("[비밀번호 변경 성공] userId: {}", user.getId());
	}

	@Override
	@Transactional
	public void updateSimplePassword(UserContext userContext, SimplePasswordVerifyReq request) {
		// DB에서 User 엔티티를 다시 조회 (영속성 컨텍스트에서 관리되는 엔티티)
		User user = userRepository.findById(userContext.getId())
			.orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

		// 간편 비밀번호 암호화 및 업데이트
		String encodedSimplePassword = passwordEncoder.encode(request.password());
		user.updateSimplePassword(encodedSimplePassword);

		log.info("[간편 비밀번호 변경 성공] userId: {}", user.getId());
	}

	@Override
	@Transactional
	public void updatePushSettings(UserContext userContext, UpdatePushReq request) {
		// DB에서 User 엔티티를 다시 조회 (영속성 컨텍스트에서 관리되는 엔티티)
		User user = userRepository.findById(userContext.getId())
			.orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

		// 제공된 필드만 업데이트 (null이 아닌 경우에만)
		if (request.pushEnabled() != null) {
			user.updatePushEnabled(request.pushEnabled());
		}
		if (request.nightPushEnabled() != null) {
			user.updateNightPushEnabled(request.nightPushEnabled());
		}

		log.info("[푸시 알림 설정 변경 성공] userId: {}, pushEnabled: {}, nightPushEnabled: {}",
			user.getId(), user.getPushEnabled(), user.getNightPushEnabled());
	}

	@Override
	public IdentityVerifyRes verifyIdentityMock(IdentityVerifyReq req) {

		if (req == null) {
			throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
		}

		// 1. 요청 Body 검증 (DTO → 추가 validator)
		IdentityValidator.validateCarrier(req.carrier());
		IdentityValidator.validatePhone(req.phoneNumber());
		IdentityValidator.validateBirth(req.birthFront(), req.birthBack());

		// 2. 성공 시 반환
		return new IdentityVerifyRes(true, "인증 완료");
	}

	@Override
	public FindEmailRes findEmail(FindEmailReq request) {
		// 생년월일 문자열(YYYYMMDD)을 LocalDate로 변환
		java.time.LocalDate birthDate = parseBirthDate(request.birthDate());

		// 전화번호, 생년월일, 이름으로 사용자 조회
		User user = userRepository.findByPhoneNumberAndBirthDateAndName(
			request.phoneNumber(),
			birthDate,
			request.name()
		).orElseThrow(() -> {
			log.warn("[ID 찾기 실패] 일치하는 사용자를 찾을 수 없습니다. phoneNumber: {}, birthDate: {}, name: {}",
				request.phoneNumber(), request.birthDate(), request.name());
			return new BusinessException(ErrorBaseCode.USER_NOT_FOUND);
		});

		log.info("[ID 찾기 성공] userId: {}, email: {}", user.getId(), user.getEmail());
		return new FindEmailRes(user.getEmail());
	}

	@Override
	public void resetPassword(ResetPasswordReq request) {
		// 생년월일 문자열(YYYYMMDD)을 LocalDate로 변환
		java.time.LocalDate birthDate = parseBirthDate(request.birthDate());

		// 이메일, 전화번호, 생년월일, 이름으로 사용자 조회
		User user = userRepository.findByEmailAndPhoneNumberAndBirthDateAndName(
			request.email(),
			request.phoneNumber(),
			birthDate,
			request.name()
		).orElseThrow(() -> {
			log.warn("[비밀번호 재설정 요청 실패] 일치하는 사용자를 찾을 수 없습니다. email: {}, phoneNumber: {}, birthDate: {}, name: {}",
				request.email(), request.phoneNumber(), request.birthDate(), request.name());
			return new BusinessException(ErrorBaseCode.USER_NOT_FOUND);
		});

		// 현재는 로그만 남기고, 추후 이메일 서비스 연동 필요
		log.info("[비밀번호 재설정 링크 발송] userId: {}, email: {}", user.getId(), user.getEmail());
		// 예시: emailService.sendPasswordResetLink(user.getEmail(), resetToken);
	}

	/**
	 * 생년월일 문자열(YYYYMMDD)을 LocalDate로 변환합니다.
	 *
	 * @param birthDateStr 생년월일 문자열 (YYYYMMDD 형식)
	 * @return LocalDate 객체
	 * @throws BusinessException 형식이 올바르지 않거나 유효하지 않은 날짜인 경우
	 */
	private java.time.LocalDate parseBirthDate(String birthDateStr) {
		if (birthDateStr == null) {
			throw new BusinessException(ErrorAuthCode.INVALID_FORMAT);
		}
		try {
			return java.time.LocalDate.parse(birthDateStr, java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
		} catch (java.time.format.DateTimeParseException e) {
			log.warn("[생년월일 파싱 실패] birthDateStr: {}", birthDateStr);
			throw new BusinessException(ErrorAuthCode.INVALID_FORMAT);
		}
	}

}
