package dev.syntax.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.auth.dto.EmailValidationReq;
import dev.syntax.domain.auth.dto.EmailValidationRes;
import dev.syntax.domain.auth.dto.LoginReq;
import dev.syntax.domain.auth.dto.LoginRes;
import dev.syntax.domain.auth.dto.SignupReq;
import dev.syntax.domain.auth.service.AuthService;
import dev.syntax.domain.auth.service.LoginService;
import dev.syntax.domain.auth.service.SignupService;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증(Auth) 및 회원가입 관련 엔드포인트를 제공하는 컨트롤러입니다.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final SignupService signupService;
	private final LoginService loginService;

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
}
