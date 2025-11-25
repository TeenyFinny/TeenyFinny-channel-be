package dev.syntax.domain.profile.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.profile.dto.UpdateProfileReq;
import dev.syntax.domain.profile.service.ProfileService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 프로필 관련 API 컨트롤러
 * <p>
 * 사용자의 프로필 정보 조회 및 수정 기능을 제공합니다.
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

	/**
	 * 프로필 정보를 수정합니다.
	 * <p>
	 * 요청 본문에 포함된 필드만 업데이트되며, null인 필드는 기존 값을 유지합니다.
	 * 현재 이름과 전화번호만 수정 가능합니다.
	 * </p>
	 *
	 * @param user 현재 인증된 사용자 정보
	 * @param request 수정할 프로필 정보 (name, phoneNumber)
	 * @return 성공 응답
	 */
	@PatchMapping("/info")
	public ResponseEntity<BaseResponse<?>> updateProfile(
		@CurrentUser UserContext user,
		@Valid @RequestBody UpdateProfileReq request
	) {
		profileService.updateProfile(user, request);
		return ApiResponseUtil.success(SuccessCode.OK);
	}

}
