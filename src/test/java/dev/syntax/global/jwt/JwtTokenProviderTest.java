package dev.syntax.global.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import dev.syntax.domain.auth.dto.UserContext;
import dev.syntax.domain.auth.service.UserContextServiceImpl;
import dev.syntax.domain.user.entity.User;

class JwtTokenProviderTest {

	private JwtTokenProvider provider;
	private UserContextServiceImpl userContextService;

	@BeforeEach
	void setUp() {
		userContextService = mock(UserContextServiceImpl.class);

		provider = new JwtTokenProvider(
			"z6BLCa71yUubJVvxoI1PLcFlec1qiwb+szYXKvGmlIAHwYX1F5WVq2jNP05AyAaQrpQw/iR7/DnkiEHOWtQvRg==",
			// base64 secret 예시
			1L,
			userContextService
		);
	}

	@Test
	void JWT_생성_및_검증_성공() {
		// given: userContext mocking
		User user = User.builder()
			.id(1L)
			.email("test@abc.com")
			.password("encodedPw")
			.role(dev.syntax.domain.user.enums.Role.PARENT)
			.build();

		UserContext context = new UserContext(user);

		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(context, null, context.getAuthorities());

		// when
		String token = provider.generateToken(authentication);

		// then
		assertThat(provider.validateToken(token)).isTrue();
	}

	@Test
	void JWT로부터_Authentication_복원() {
		// given
		User user = User.builder()
			.id(1L)
			.email("test@naver.com")
			.password("encodedPw")
			.role(dev.syntax.domain.user.enums.Role.PARENT)
			.build();

		UserContext context = new UserContext(user);

		when(userContextService.loadUserById(1L)).thenReturn(context);

		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(context, null, context.getAuthorities());

		String token = provider.generateToken(authentication);

		// when
		var restoredAuth = provider.getAuthentication(token);

		// then
		assertThat(restoredAuth.getPrincipal()).isInstanceOf(UserContext.class);
		UserContext restoredUser = (UserContext)restoredAuth.getPrincipal();

		assertThat(restoredUser.getId()).isEqualTo(1L);
		assertThat(restoredUser.getRole()).isEqualTo("PARENT");
	}
}
