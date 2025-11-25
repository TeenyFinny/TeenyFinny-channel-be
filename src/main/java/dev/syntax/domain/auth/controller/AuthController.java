package dev.syntax.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.auth.dto.EmailValidationReq;
import dev.syntax.domain.auth.dto.EmailValidationRes;
import dev.syntax.domain.auth.dto.IdentityVerifyReq;
import dev.syntax.domain.auth.dto.IdentityVerifyRes;
import dev.syntax.domain.auth.dto.LoginReq;
import dev.syntax.domain.auth.dto.LoginRes;
import dev.syntax.domain.auth.dto.OtpGenerateRes;
import dev.syntax.domain.auth.dto.OtpVerifyReq;
import dev.syntax.domain.auth.dto.OtpVerifyRes;
import dev.syntax.domain.auth.dto.PasswordVerifyReq;
import dev.syntax.domain.auth.dto.PasswordVerifyRes;
import dev.syntax.domain.auth.dto.RefreshTokenRes;
import dev.syntax.domain.auth.dto.SignupReq;
import dev.syntax.domain.auth.dto.SimplePasswordVerifyReq;
import dev.syntax.domain.auth.dto.UpdatePasswordReq;
import dev.syntax.domain.auth.service.AuthService;
import dev.syntax.domain.auth.service.FamilyService;
import dev.syntax.domain.auth.service.LoginService;
import dev.syntax.domain.auth.service.SignupService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final SignupService signupService;
	private final LoginService loginService;
	private final FamilyService familyService;

	/**
	 * 회원가입을 수행합니다.
	 *
	 * @param signupReq 회원가입 요청 DTO
	 */
	@PostMapping("/signup")
	public ResponseEntity<BaseResponse<?>> signup(@Valid @RequestBody SignupReq signupReq) {
		signupService.signup(signupReq);
		return ApiResponseUtil.success(SuccessCode.CREATED);
	}

	/**
	 * 이메일/비밀번호 로그인 엔드포인트
	 *
	 * @param request 로그인 요청 DTO
	 * @return 표준 응답 포맷으로 래핑된 로그인 응답 DTO
	 */
	@PostMapping("/login")
	public ResponseEntity<BaseResponse<?>> login(@Valid @RequestBody LoginReq request) {
		LoginRes response = loginService.login(request);
		return ApiResponseUtil.success(SuccessCode.OK, response);
	}

	/**
	 * 이메일 중복 여부를 확인합니다.
	 *
	 * @param req 이메일 검증 요청 DTO
	 */
	@PostMapping("/email")
	public ResponseEntity<BaseResponse<?>> verifyEmail(
		@Valid @RequestBody EmailValidationReq req
	) {
		authService.checkEmailDuplicate(req);
		return ApiResponseUtil.success(SuccessCode.OK, new EmailValidationRes(true));
	}

	/**
	 * 사용자의 최신 정보로 새로운 JWT 토큰을 발급합니다.
	 * 가족 관계 변경 등으로 UserContext 업데이트가 필요할 때 사용합니다.
	 *
	 * @param context 인증된 사용자 컨텍스트
	 * @return 새로운 JWT 토큰
	 */
	@GetMapping("/refresh")
	public ResponseEntity<BaseResponse<?>> refreshToken(@CurrentUser UserContext context) {
		RefreshTokenRes response = authService.refreshToken(context.getId());
		log.info("[토큰 갱신 완료] user_id: {}", context.getId());
		return ApiResponseUtil.success(SuccessCode.OK, response);
	}

	/**
	 * 부모 사용자를 위한 OTP를 생성합니다.
	 *
	 * @param context 인증된 사용자 컨텍스트
	 * @return OTP 생성 응답
	 */
	@GetMapping("/otp")
	public ResponseEntity<BaseResponse<?>> generateOtp(@CurrentUser UserContext context) {
		OtpGenerateRes response = familyService.generateOtp(context.getId());
		log.info("[OTP 발급 완료] user_id: {}", context.getId());
		return ApiResponseUtil.success(SuccessCode.OK, response);
	}

	/**
	 * 자녀 사용자가 입력한 OTP를 검증하고 가족 관계를 생성합니다.
	 *
	 * @param context 인증된 사용자 컨텍스트
	 * @param request OTP 검증 요청
	 * @return OTP 검증 응답
	 */
	@PostMapping("/otp")
	public ResponseEntity<BaseResponse<?>> verifyOtp(
		@CurrentUser UserContext context,
		@Valid @RequestBody OtpVerifyReq request
	) {
		OtpVerifyRes response = familyService.verifyOtpAndCreateRelationship(context.getId(), request);
		log.info("[가족 등록 완료] child_id: {}, parent_id: {}", response.userId(), response.parentId());
		return ApiResponseUtil.success(SuccessCode.OK, response);
	}

	/**
	 * 비밀번호 인증을 수행합니다.
	 *
	 * @param context 인증된 사용자
	 * @param req 비밀번호 인증 요청 DTO
	 */
	@PostMapping("/password")
	public ResponseEntity<BaseResponse<?>> verifyPassword(
		@CurrentUser UserContext context,
		@Valid @RequestBody PasswordVerifyReq req
	) {
		PasswordVerifyRes response = authService.verifyPassword(context.getId(), req);
		return ApiResponseUtil.success(SuccessCode.OK, response);
	}

	/**
	 * 간편비밀번호(Simple Password)를 검증하는 엔드포인트
	 *
	 * @param context 인증된 사용자 컨텍스트
	 * @param req 사용자 입력 간편비밀번호 요청 DTO
	 */
	@PostMapping("/simple-password")
	public ResponseEntity<BaseResponse<?>> verifySimplePassword(
		@CurrentUser UserContext context,
		@Valid @RequestBody SimplePasswordVerifyReq req
	) {
		PasswordVerifyRes response = authService.verifySimplePassword(context.getId(), req);
		return ApiResponseUtil.success(SuccessCode.OK, response);
	}

	/**
	 * 비밀번호를 변경합니다.
	 * <p>
	 * 현재 비밀번호를 검증한 후 새 비밀번호로 변경합니다.
	 * 새 비밀번호는 8자리 이상이며 특수문자를 포함해야 합니다.
	 * </p>
	 *
	 * @param user 현재 인증된 사용자 정보
	 * @param request 현재 비밀번호와 새 비밀번호
	 * @return 성공 응답
	 */
	@PatchMapping("/password")
	public ResponseEntity<BaseResponse<?>> updatePassword(
		@CurrentUser UserContext user,
		@Valid @RequestBody UpdatePasswordReq request
	) {
		authService.updatePassword(user, request);
		return ApiResponseUtil.success(SuccessCode.OK);
	}

	/**
	 * 간편 비밀번호를 변경합니다.
	 * <p>
	 * 6자리 숫자로 구성된 간편 비밀번호를 변경합니다.
	 * </p>
	 *
	 * @param user 현재 인증된 사용자 정보
	 * @param request 새로운 간편 비밀번호 (6자리 숫자)
	 * @return 성공 응답
	 */
	@PatchMapping("/simple-password")
	public ResponseEntity<BaseResponse<?>> updateSimplePassword(
		@CurrentUser UserContext user,
		@Valid @RequestBody SimplePasswordVerifyReq request
	) {
		authService.updateSimplePassword(user, request);
		return ApiResponseUtil.success(SuccessCode.OK);
	}

	/**
	 * 푸시 알림 설정을 변경합니다.
	 * <p>
	 * 푸시 알림과 야간 푸시 알림 설정을 변경합니다.
	 * 요청 본문에 포함된 필드만 업데이트되며, null인 필드는 기존 값을 유지합니다.
	 * </p>
	 *
	 * @param user 현재 인증된 사용자 정보
	 * @param request 푸시 알림 설정 (pushEnabled, nightPushEnabled)
	 * @return 성공 응답
	 */
	@PatchMapping("/push")
	public ResponseEntity<BaseResponse<?>> updatePushSettings(
		@CurrentUser UserContext user,
		@Valid @RequestBody dev.syntax.domain.auth.dto.UpdatePushReq request
	) {
		authService.updatePushSettings(user, request);
		return ApiResponseUtil.success(SuccessCode.OK);
	}

	/**
	 * 사용자의 본인 인증 정보를 검증합니다.
	 *
	 * <p>요청된 정보와 DB에 저장된 사용자 정보를 비교하여 인증을 수행합니다.
	 * 이름, 전화번호, 생년월일 앞자리, 주민번호 뒷자리까지 모두 일치해야 인증이 성공합니다.
	 *
	 * @param context 인증된 사용자 컨텍스트
	 * @param request 본인 인증 요청 DTO
	 * @return 인증 성공 여부 및 메시지를 담은 {@link IdentityVerifyRes}를 표준 응답 포맷으로 래핑
	 */
	@PostMapping("/identity")
	public ResponseEntity<BaseResponse<?>> verifyIdentity(
		@CurrentUser UserContext context,
		@Valid @RequestBody IdentityVerifyReq request
	) {
		IdentityVerifyRes response = authService.verifyIdentity(context.getId(), request);
		return ApiResponseUtil.success(SuccessCode.OK, response);
	}

}
