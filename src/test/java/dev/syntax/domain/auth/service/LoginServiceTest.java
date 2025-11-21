package dev.syntax.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import dev.syntax.domain.auth.dto.LoginReq;
import dev.syntax.domain.auth.dto.LoginRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.auth.jwt.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@InjectMocks
	private LoginServiceImpl loginService;

	@Test
	@DisplayName("로그인 성공 시 토큰과 사용자 정보를 반환한다")
	void login_success() {
		// given
		LoginReq request = new LoginReq("test@teenyfinny.io", "password");
		User user = User.builder()
			.id(1L)
			.email("test@teenyfinny.io")
			.password("encoded")
			.role(Role.PARENT)
			.build();
		UserContext userContext = new UserContext(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userContext, null, userContext.getAuthorities()
		);

		when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
		when(jwtTokenProvider.generateToken(authentication)).thenReturn("mocked-jwt-token");

		// when
		LoginRes response = loginService.login(request);

		// then
		assertThat(response.accessToken()).isEqualTo("mocked-jwt-token");
		assertThat(response.user().userId()).isEqualTo(1L);
		assertThat(response.user().role()).isEqualTo("PARENT");
	}
}

