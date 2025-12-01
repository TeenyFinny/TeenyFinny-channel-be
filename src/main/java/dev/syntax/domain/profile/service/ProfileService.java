package dev.syntax.domain.profile.service;

import dev.syntax.domain.profile.dto.ProfileInfoRes;
import dev.syntax.domain.profile.dto.UpdateProfileReq;
import dev.syntax.global.auth.dto.UserContext;

/**
 * 프로필 서비스 인터페이스
 * <p>
 * 사용자 프로필 정보 조회 및 수정 기능을 정의합니다.
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

	/**
	 * 사용자의 프로필 정보를 수정합니다.
	 * <p>
	 * 요청 DTO에 포함된 필드만 업데이트되며, null인 필드는 기존 값을 유지합니다.
	 * 현재 이름과 전화번호만 수정 가능합니다.
	 * </p>
	 *
	 * @param user 현재 인증된 사용자 정보
	 * @param request 수정할 프로필 정보 (name, phoneNumber)
	 */
	void updateProfile(UserContext user, UpdateProfileReq request);
}
