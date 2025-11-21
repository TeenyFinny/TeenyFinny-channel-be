package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.SignupReq;

/**
 * 회원가입(Sign-up) 기능을 담당하는 서비스 인터페이스입니다.
 * <p>
 * 사용자 생성 로직을 정의하며, 입력된 회원정보를 기반으로
 * 사용자 엔티티를 생성하고 저장하는 과정을 수행합니다.
 * 인증/인가(Auth) 도메인의 회원 생성 책임을 분리하여 관리합니다.
 */
public interface SignupService {
	
	/**
	 * 회원가입을 수행합니다.
	 *
	 * @param inputUser 회원가입 요청 DTO
	 */
	void signup(SignupReq inputUser);
}
