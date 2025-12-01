package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.LoginRes;
import dev.syntax.domain.auth.dto.oauth.KakaoLoginReq;
import dev.syntax.domain.auth.dto.oauth.KakaoLoginRes;
import dev.syntax.domain.auth.dto.oauth.KakaoSignupReq;

/**
 * 카카오 OAuth 인증 서비스 인터페이스
 */
public interface KakaoOAuthService {

	/**
	 * 카카오 로그인 처리
	 * 
	 * @param request 카카오 로그인 요청 (code, redirectUri)
	 * @return 기존 사용자: JWT 토큰, 신규 사용자: 임시 토큰
	 */
	KakaoLoginRes loginWithKakao(KakaoLoginReq request);

	/**
	 * 카카오 신규 회원가입 처리
	 * 
	 * @param request 카카오 회원가입 요청 (tempToken, 추가 정보)
	 * @return JWT 토큰 및 사용자 정보
	 */
	LoginRes signupWithKakao(KakaoSignupReq request);
}
