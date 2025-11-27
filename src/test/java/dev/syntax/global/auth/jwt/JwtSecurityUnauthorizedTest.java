package dev.syntax.global.auth.jwt;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.auth.jwt.test.TestAuthenticationFactory;
import dev.syntax.global.auth.service.UserContextServiceImpl;
import dev.syntax.global.response.AuthErrorResponse;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;

@SpringBootTest
@AutoConfigureMockMvc
@Import(JwtSecurityUnauthorizedTest.TestConfig.class)
class JwtSecurityUnauthorizedTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private UserContextServiceImpl userContextService;  // 🔹 우리가 직접 만든 Mock 빈이 주입됨

	@TestConfiguration
	static class TestConfig {

		/** 1. UserContextServiceImpl Mock Bean 생성 */
		@Bean
		public UserContextServiceImpl userContextService() {
			return mock(UserContextServiceImpl.class);
		}

		/** 2. 테스트용 JwtTokenProvider Bean 생성 */
		@Bean
		public JwtTokenProvider jwtTokenProvider(UserContextServiceImpl userContextService) {
			String testSecret = "z6BLCa71yUubJVvxoI1PLcFlec1qiwb+szYXKvGmlIAHwYX1F5WVq2jNP05AyAaQrpQw/iR7/DnkiEHOWtQvRg=="; // base64
			long expirationDays = 1L;

			return new JwtTokenProvider(
				testSecret,
				expirationDays,
				userContextService
			);
		}
	}

	/**
	 * JWT 없이 인증이 필요한 API에 접근할 때
	 * 401 Unauthorized를 반환하는지 테스트합니다.
	 */
	@Test
	@DisplayName("JWT 없이 인증 필요 API 접근 → 401 Unauthorized")
	void unauthorizedWithoutJwt() throws Exception {

		BaseResponse<?> expected = AuthErrorResponse.of(ErrorAuthCode.UNAUTHORIZED);
		String expectedJson = objectMapper.writeValueAsString(expected);

		mockMvc.perform(get("/test/secure")
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized())
			.andExpect(content().json(expectedJson));
	}

	/**
	 * 유효한 JWT로 인증이 필요한 API에 접근할 때
	 * 200 OK를 반환하는지 테스트합니다.
	 */
	@Test
	@DisplayName("유효한 JWT로 인증 필요 API 접근 → 200 OK")
	void authorizedWithJwt() throws Exception {

		var auth = TestAuthenticationFactory.createAuth();
		String token = jwtTokenProvider.generateToken(auth);

		// 🔹 DB 없이 UserContext 복구 Mocking
		UserContext context = (UserContext) auth.getPrincipal();
		when(userContextService.loadUserById(1L)).thenReturn(context);

		mockMvc.perform(get("/test/secure")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(content().string("OK"));
	}
}