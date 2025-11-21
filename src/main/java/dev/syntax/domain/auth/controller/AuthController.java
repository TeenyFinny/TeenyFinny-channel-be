package dev.syntax.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.auth.dto.EmailValidationReq;
import dev.syntax.domain.auth.dto.EmailValidationRes;
import dev.syntax.domain.auth.dto.SignupReq;
import dev.syntax.domain.auth.service.AuthService;
import dev.syntax.domain.auth.service.SignupService;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthController
 *
 * <p>/auth 영역의 인증 관련 엔드포인트를 담당합니다.</p>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final SignupService signupService;

	@PostMapping("/signup")
	public ResponseEntity<BaseResponse<?>> signup(@Valid @RequestBody SignupReq signupReq) {
		signupService.signup(signupReq);
		return ApiResponseUtil.success(SuccessCode.CREATED);
	}

	@PostMapping("/email")
	public ResponseEntity<BaseResponse<?>> verifyEmail(
		@Valid @RequestBody EmailValidationReq req
	) {
		authService.checkEmailDuplicate(req);
		return ApiResponseUtil.success(SuccessCode.OK, new EmailValidationRes(true));
	}
}
