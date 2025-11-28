package dev.syntax.global.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import dev.syntax.global.auth.jwt.JwtAccessDeniedHandler;
import dev.syntax.global.auth.jwt.JwtAuthenticationEntryPoint;
import dev.syntax.global.auth.jwt.JwtAuthenticationFilter;
import dev.syntax.global.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

/**
 * SecurityConfig
 *
 * <p>Spring Security의 전역 보안 설정을 담당하는 구성 클래스입니다.
 * 본 서비스는 JWT 기반의 인증 방식을 사용하기 때문에
 * 세션을 생성하지 않는 Stateless 환경을 기본 정책으로 구성합니다.
 *
 * <h2>인증 흐름 요약</h2>
 * <ol>
 *     <li>클라이언트가 HTTP Authorization 헤더로 JWT 토큰을 전송합니다.</li>
 *     <li>JwtAuthenticationFilter가 요청을 가로채 토큰 유효성 검증을 수행합니다.</li>
 *     <li>유효한 토큰이면 SecurityContext에 Authentication(UserContext 기반)을 저장합니다.</li>
 *     <li>이후 컨트롤러 계층까지 인증 정보가 전달되며, 인가 검사가 적용됩니다.</li>
 * </ol>
 *
 * @see JwtAuthenticationFilter
 * @see JwtTokenProvider
 * @see JwtAuthenticationEntryPoint
 * @see JwtAccessDeniedHandler
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	@Value("${cors.allowed-origin-patterns}")
	private String[] allowedOriginPatterns;

	/**
	 * SecurityFilterChain을 구성합니다.
	 *
	 * <p>JWT 기반 인증 구조에서는 서버가 세션을 유지하지 않으므로
	 * SessionCreationPolicy.STATELESS 정책을 설정합니다.
	 * UsernamePasswordAuthenticationFilter 이전에 JwtAuthenticationFilter를 등록하여
	 * 모든 요청에서 JWT 유효성 검증을 먼저 수행합니다.
	 * JWT API 서버에는 맞지 않은 규칙 SonarQube 오탐 발생
	 *
	 * @param http HttpSecurity 보안 설정 DSL
	 * @return SecurityFilterChain 빌드된 보안 필터 체인
	 * @throws Exception 설정 중 발생할 수 있는 예외
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// .csrf(csrf -> csrf.disable())
			.csrf(csrf -> csrf
				.ignoringRequestMatchers("/**")   // JWT 기반 API 서버는 모든 요청에서 CSRF 미사용
				.disable()
			)

			// CORS 설정 적용
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))

			// 인증 및 인가 관련 예외 처리기 등록
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				.accessDeniedHandler(jwtAccessDeniedHandler)
			)

			// 세션을 사용하지 않는 Stateless 정책 적용
			.sessionManagement(sessionManagement -> sessionManagement
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)

			// 경로별 인증 여부 설정
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers(
					"/auth/**",
					"/public/**",
					"/sample/**",
					"/docs/**").permitAll()
				.anyRequest().authenticated()
			)
			// UsernamePasswordAuthenticationFilter 전에 JWT 필터 등록
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
				UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * BCryptPasswordEncoder Bean 등록
	 *
	 * <p>bcrypt는 자체적으로 Salt를 내장하고 있으며,
	 * 비밀번호 검증 시 matches() 호출만으로 비교가 가능합니다.
	 *
	 * @return BCryptPasswordEncoder 인스턴스
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * AuthenticationManager Bean 등록
	 *
	 * <p>Spring Security 내부 인증 프로세스를 수행하는 핵심 컴포넌트입니다.
	 * AuthService에서 이메일·비밀번호 로그인 시 AuthenticationManager를 호출하여
	 * UserDetailsService, PasswordEncoder 등을 통한 인증 절차가 수행됩니다.</p>
	 *
	 * @param configuration AuthenticationManager 설정 정보
	 * @return AuthenticationManager 인스턴스
	 * @throws Exception 설정 오류 발생 시
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	/**
	 * CORS 설정을 위한 Bean 등록
	 *
	 * <p>허용된 출처(Origin), 메소드, 헤더 및 자격 증명(Credentials)을 설정합니다.
	 * 모든 경로("/**")에 대해 해당 정책을 적용합니다.</p>
	 *
	 * @return CorsConfigurationSource CORS 설정 소스
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(Arrays.asList(allowedOriginPatterns));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
