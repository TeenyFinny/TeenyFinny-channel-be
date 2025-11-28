package dev.syntax.global.auth.jwt.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JWT 필터 및 Spring Security 설정 검증을 위한 테스트용 API입니다.
 * 인증이 필요한 경로이며, 실제 도메인과 무관합니다.
 */
@RestController
public class SecurityTestController {

	@GetMapping("/test/secure")
	public String secureEndpoint() {
		return "OK";
	}
}