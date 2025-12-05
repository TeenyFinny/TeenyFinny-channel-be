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
import org.springframework.security.authentication.BadCredentialsException;
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

	/**
	 * TC-AUTH-002: 로그인 실패 - 잘못된 비밀번호 입력 테스트
	 * 
	 * <p>테스트 시나리오:</p>
	 * <ul>
	 *   <li>존재하는 이메일이지만 잘못된 비밀번호로 로그인 시도</li>
	 *   <li>AuthenticationManager에서 BadCredentialsException 발생</li>
	 *   <li>예외가 LoginService에서 처리되지 않고 그대로 전파되는지 확인</li>
	 * </ul>
	 */
	@Test
	@DisplayName("로그인 실패 - 잘못된 비밀번호 입력")
	void login_failure_invalid_password() {
		// given: 잘못된 비밀번호로 로그인 요청
		LoginReq request = new LoginReq("test@teenyfinny.io", "wrong-password");
		
		// AuthenticationManager가 인증 실패 시 BadCredentialsException을 던지도록 설정
		// Spring Security는 비밀번호가 일치하지 않으면 이 예외를 발생시킴
		when(authenticationManager.authenticate(any(Authentication.class)))
			.thenThrow(new BadCredentialsException("자격 증명에 실패하였습니다."));

		// when & then: 로그인 시도 시 BadCredentialsException 발생 확인
		assertThatThrownBy(() -> loginService.login(request))
			.isInstanceOf(BadCredentialsException.class)
			.hasMessageContaining("자격 증명에 실패하였습니다.");
		
		// verify: AuthenticationManager.authenticate()가 호출되었는지 확인
		verify(authenticationManager, times(1)).authenticate(any(Authentication.class));
		
		// verify: 인증 실패 시 토큰 생성이 호출되지 않았는지 확인
		verify(jwtTokenProvider, never()).generateToken(any(Authentication.class));
	}

}
