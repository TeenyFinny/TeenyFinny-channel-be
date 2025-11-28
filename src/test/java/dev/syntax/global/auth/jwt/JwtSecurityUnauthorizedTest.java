package dev.syntax.global.auth.jwt;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.auth.jwt.test.SecurityTestController;
import dev.syntax.global.auth.jwt.test.TestAuthenticationFactory;
import dev.syntax.global.auth.service.UserContextServiceImpl;
import dev.syntax.global.response.AuthErrorResponse;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;

@WebMvcTest(controllers = SecurityTestController.class)
@Import(JwtSecurityUnauthorizedTest.TestConfig.class)
@ActiveProfiles("test")
class JwtSecurityUnauthorizedTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private UserContextServiceImpl userContextService;

	@TestConfiguration
	static class TestConfig {

		@Bean
		public UserContextServiceImpl userContextService() {
			return mock(UserContextServiceImpl.class);
		}

		@Bean
		public JwtTokenProvider jwtTokenProvider(UserContextServiceImpl userContextService) {
			String testSecret =
				"z6BLCa71yUubJVvxoI1PLcFlec1qiwb+szYXKvGmlIAHwYX1F5WVq2jNP05AyAaQrpQw/iR7/DnkiEHOWtQvRg==";
			return new JwtTokenProvider(testSecret, 1L, userContextService);
		}

		@Bean
		public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
			return new JwtAuthenticationEntryPoint(objectMapper);
		}

		@Bean
		public SecurityFilterChain testSecurityFilterChain(
			HttpSecurity http,
			JwtTokenProvider jwtTokenProvider,
			JwtAuthenticationEntryPoint entryPoint
		) throws Exception {

			http
				.csrf(csrf -> csrf.disable())

				.exceptionHandling(ex -> ex
					.authenticationEntryPoint(entryPoint)
				)

				.authorizeHttpRequests(auth -> auth
					.requestMatchers("/test/**").authenticated()
					.anyRequest().permitAll()
				)

				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
					UsernamePasswordAuthenticationFilter.class);

			return http.build();
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