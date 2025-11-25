package dev.syntax.domain.profile.service;

import dev.syntax.domain.profile.dto.ProfileInfoRes;
import dev.syntax.global.auth.dto.UserContext;

/**
 * 프로필 서비스 인터페이스
 * <p>
 * 사용자 프로필 정보 조회 기능을 정의합니다.
 * </p>
 */
public interface ProfileService {
	
	/**
	 * 사용자의 프로필 정보를 조회합니다.
	 *
	 * @param user 현재 인증된 사용자 정보
	 * @return 프로필 정보 응답 DTO
	 */
	ProfileInfoRes profileInfo(UserContext user);
}
