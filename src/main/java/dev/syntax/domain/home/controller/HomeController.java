package dev.syntax.domain.home.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.home.dto.HomeRes;
import dev.syntax.domain.home.service.HomeService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 홈 화면 관련 API 요청을 처리하는 컨트롤러입니다.
 */
@Slf4j
@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
public class HomeController {

	private final HomeService homeService;

	/**
	 * 홈 화면 데이터를 조회합니다.
	 *
	 * @param user 인증된 사용자 컨텍스트
	 * @return 홈 화면 데이터 응답
	 */
	@GetMapping
	public ResponseEntity<BaseResponse<?>> getHome(@CurrentUser UserContext user) {
		HomeRes response = homeService.getHomeData(user.getId());
		log.info("홈 화면 조회 성공: userId = {}, userRole = {}", user.getId(), user.getRole());
		return ApiResponseUtil.success(SuccessCode.OK, response);
	}
}
