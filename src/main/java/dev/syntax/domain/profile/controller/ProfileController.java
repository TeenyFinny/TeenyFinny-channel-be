package dev.syntax.domain.profile.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.profile.service.ProfileService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;

/**
 * 프로필 관련 API 컨트롤러
 * <p>
 * 사용자의 프로필 정보 조회 기능을 제공합니다.
 * </p>
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

	private final ProfileService profileService;

	/**
	 * 프로필 정보를 조회합니다.
	 * <p>
	 * 현재 로그인한 사용자의 이름, 이메일, 전화번호를 반환합니다.
	 * </p>
	 *
	 * @param user 현재 인증된 사용자 정보
	 * @return 프로필 정보 응답
	 */
	@GetMapping("/info")
	public ResponseEntity<BaseResponse<?>> getProfileInfo(@CurrentUser UserContext user) {
		return ApiResponseUtil.success(SuccessCode.OK, profileService.profileInfo(user));
	}

}
