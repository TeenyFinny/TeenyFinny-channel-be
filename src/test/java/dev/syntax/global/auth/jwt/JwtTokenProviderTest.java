package dev.syntax.global.auth.jwt;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import dev.syntax.domain.user.entity.User;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.auth.service.UserContextServiceImpl;

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

	/**
	 * 토큰 갱신 기능 테스트
	 * 
	 * <p>테스트 시나리오:</p>
	 * <ul>
	 *   <li>기존 사용자 정보로 첫 번째 JWT 토큰 생성</li>
	 *   <li>기존 토큰에서 Authentication을 복원</li>
	 *   <li>복원된 Authentication으로 새로운 토큰 생성 (토큰 갱신)</li>
	 *   <li>새로 생성된 토큰의 유효성 검증</li>
	 *   <li>새 토큰에서 복원한 사용자 정보가 원본과 동일한지 확인</li>
	 * </ul>
	 * 
	 * <p>이 테스트는 GET /auth/refresh 엔드포인트의 핵심 로직인
	 * JWT 토큰 갱신 기능이 올바르게 동작하는지 검증합니다.</p>
	 */
	@Test
	void 토큰_갱신_성공() {
		// given: 기존 사용자 정보로 첫 번째 토큰 생성
		// 부모 사용자 엔티티 생성
		User user = User.builder()
			.id(2L)
			.email("test@teenyfinny.io")
			.password("encodedPw")
			.role(dev.syntax.domain.user.enums.Role.PARENT)
			.build();

		// UserContext 생성 (Spring Security의 Principal 역할)
		UserContext context = new UserContext(user);

		// UserContextService가 사용자 ID로 UserContext를 반환하도록 Mock 설정
		when(userContextService.loadUserById(2L)).thenReturn(context);

		// Authentication 객체 생성 (실제 로그인 시 생성되는 객체와 동일한 형태)
		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(context, null, context.getAuthorities());

		// 첫 번째 JWT 토큰 생성 (로그인 시 발급되는 토큰)
		String originalToken = provider.generateToken(authentication);

		// when: 기존 토큰에서 Authentication을 복원하고 새로운 토큰 생성 (토큰 갱신 시뮬레이션)
		// 기존 토큰을 파싱하여 Authentication 복원
		var restoredAuth = provider.getAuthentication(originalToken);
		
		// 복원된 Authentication으로 새로운 토큰 생성 (토큰 갱신)
		String refreshedToken = provider.generateToken(restoredAuth);

		// then: 새로 생성된 토큰이 유효한지 검증
		// 1. 갱신된 토큰이 유효한 JWT 형식이고 서명이 올바른지 확인
		assertThat(provider.validateToken(refreshedToken)).isTrue();
		
		// 2. 새 토큰에서 복원한 사용자 정보가 원본과 동일한지 검증
		var refreshedAuth = provider.getAuthentication(refreshedToken);
		UserContext refreshedUser = (UserContext) refreshedAuth.getPrincipal();
		
		// 사용자 ID가 동일한지 확인
		assertThat(refreshedUser.getId()).isEqualTo(2L);
		// 이메일이 동일한지 확인
		assertThat(refreshedUser.getEmail()).isEqualTo("test@teenyfinny.io");
		// 역할(권한)이 동일한지 확인
		assertThat(refreshedUser.getRole()).isEqualTo("PARENT");
		
		// 원본 토큰과 갱신된 토큰은 다른 값이어야 함 (새로 생성되었으므로)
		assertThat(refreshedToken).isNotEqualTo(originalToken);
	}
}
