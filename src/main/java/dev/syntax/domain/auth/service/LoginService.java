package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.LoginReq;
import dev.syntax.domain.auth.dto.LoginRes;

/**
 * 로그인(Authentication) 기능을 담당하는 서비스 인터페이스입니다.
 *
 * <p>이메일과 비밀번호를 기반으로 사용자를 인증하고,
 * 인증에 성공하면 JWT 액세스 토큰을 발급하는 도메인 로직을 정의합니다.
 * 인증/인가(Auth) 도메인에서 사용자 검증과 토큰 발급 책임을 분리하여 관리합니다.
 */
public interface LoginService {
	
	/**
	 * 로그인 요청을 처리합니다.
	 *
	 * @param request 이메일/비밀번호 기반 로그인 요청 DTO
	 * @return 발급된 JWT 토큰과 사용자 정보를 포함한 응답 DTO
	 */
	LoginRes login(LoginReq request);
}
