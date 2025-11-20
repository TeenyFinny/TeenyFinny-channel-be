package dev.syntax.global.jwt;

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

import dev.syntax.domain.auth.dto.UserContext;
import dev.syntax.domain.auth.service.UserContextServiceImpl;
import dev.syntax.global.jwt.test.TestAuthenticationFactory;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;

@SpringBootTest
@AutoConfigureMockMvc
@Import(JwtSecurityUnauthorizedTest.MockConfig.class)
class JwtSecurityUnauthorizedTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private UserContextServiceImpl userContextService; // 🔹 이제 MockBean 아님, MockConfig에서 주입됨

	@TestConfiguration
	static class MockConfig {
		@Bean
		public UserContextServiceImpl userContextService() {
			return mock(UserContextServiceImpl.class);
		}
	}

	/**
	 * JWT 없이 인증이 필요한 API에 접근할 때
	 * 401 Unauthorized를 반환하는지 테스트합니다.
	 */
	@Test
	@DisplayName("JWT 없이 인증 필요 API 접근 → 401 Unauthorized")
	void unauthorizedWithoutJwt() throws Exception {

		BaseResponse<?> expected = BaseResponse.of(ErrorAuthCode.UNAUTHORIZED);
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

		// 🔹 Mocking (DB 대신)
		UserContext context = (UserContext)auth.getPrincipal();
		when(userContextService.loadUserById(1L)).thenReturn(context);

		mockMvc.perform(get("/test/secure")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(content().string("OK"));
	}
}
