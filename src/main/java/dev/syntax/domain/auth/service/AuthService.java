package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.EmailValidationReq;
import dev.syntax.domain.auth.dto.RefreshTokenRes;
import dev.syntax.global.exception.BusinessException;

/**
 * 인증(Authentication)과 인가(Authorization)에 필요한
 * 보안 관련 도메인 로직을 제공하는 Service 인터페이스입니다.
 *
 * <p>토큰 및 비밀번호 검증 기능을 포함하며,
 * 회원가입 과정에서 필요한 이메일 중복 확인 기능도 제공합니다.
 */
public interface AuthService {

	/**
	 * 이메일 중복 여부를 검증합니다.
	 * <p>
	 * 전달된 이메일이 이미 사용자 테이블에 존재할 경우
	 * {@link BusinessException}을 발생시켜 가입을 차단합니다.
	 *
	 * @param request 이메일 중복 검증 요청 DTO
	 * @throws BusinessException 이메일이 이미 존재하는 경우 발생
	 */
	void checkEmailDuplicate(EmailValidationReq request);

	/**
	 * 사용자의 최신 정보로 새로운 JWT 토큰을 발급합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 새로운 JWT 토큰
	 */
	RefreshTokenRes refreshToken(Long userId);
}

