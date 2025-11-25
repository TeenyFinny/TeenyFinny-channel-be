package dev.syntax.domain.account.controller;

import dev.syntax.domain.account.dto.CreateChildAccountReq;
import dev.syntax.domain.account.service.BankAccountService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 용돈 계좌 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/allowance")
@RequiredArgsConstructor
public class AllowanceController {

	private final BankAccountService bankAccountService;

	/**
	 * 자녀의 용돈 계좌를 생성합니다.
	 * @param userContext 현재 사용자 정보
	 * @param req 자녀 계좌 생성 요청
	 * @return 성공 응답
	 */
	@PostMapping("/accounts")
	public ResponseEntity<?> createAccount(@CurrentUser UserContext userContext,
		@RequestBody CreateChildAccountReq req) {
		bankAccountService.createChildAllowanceAccount(userContext.getUser(), req);
		return ApiResponseUtil.success(SuccessCode.CREATED);
	}
}
