package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.EmailValidationReq;
import dev.syntax.global.exception.BusinessException;

/**
 * 인증(Authentication) 및 인가(Authorization)와 관련된
 * 도메인 로직을 제공하는 Service 인터페이스입니다.
 * <p>
 * 회원가입, 로그인, 토큰 검증 등 보안 관련 기능을 처리하며,
 * 그 중 이메일 중복 여부 검증은 회원가입 과정에서 필요한
 * 사전 확인 절차로 제공됩니다.
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
}

