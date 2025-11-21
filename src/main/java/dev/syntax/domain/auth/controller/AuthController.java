package dev.syntax.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.auth.dto.EmailValidationReq;
import dev.syntax.domain.auth.dto.EmailValidationRes;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import dev.syntax.global.response.error.ErrorAuthCode;
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

	private final UserRepository userRepository;

	@PostMapping("/email")
	public ResponseEntity<BaseResponse<?>> verifyEmail(
		@Valid @RequestBody EmailValidationReq req
	) {
		boolean exists = userRepository.findByEmail(req.email()).isPresent();
		// 이미 존재
		if (exists) {
			return ApiResponseUtil.failure(ErrorAuthCode.EMAIL_CONFLICT);
		}
		return ApiResponseUtil.success(SuccessCode.OK, new EmailValidationRes(true));
	}
}
