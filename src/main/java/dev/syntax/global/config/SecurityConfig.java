package dev.syntax.global.config;

import dev.syntax.global.jwt.JwtAccessDeniedHandler;
import dev.syntax.global.jwt.JwtAuthenticationEntryPoint;
import dev.syntax.global.jwt.JwtAuthenticationFilter;
import dev.syntax.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정을 담당하는 클래스입니다.
 * JWT 기반 인증을 적용하기 위해 필요한 필터, 예외 처리기, 세션 정책 등을 정의합니다.
 *
 * 주요 기능:
 * - CSRF 비활성화
 * - 인증 실패(401), 인가 실패(403)에 대한 커스텀 핸들러 적용
 * - 세션을 사용하지 않는 Stateless 환경 설정
 * - 요청 경로별 접근 권한 설정
 * - UsernamePasswordAuthenticationFilter 이전에 JWT 인증 필터 적용
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * SecurityFilterChain을 구성합니다.
     * JWT 기반 인증을 사용하므로 세션을 생성하지 않고,
     * 인증이 필요한 요청에 대해 JwtAuthenticationFilter가 먼저 실행되도록 설정합니다.
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 설정 중 발생한 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT 기반 인증에서는 CSRF가 필요하지 않음
                .csrf(csrf -> csrf.disable())

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
                        .requestMatchers(
                                "/auth/**",
                                "/public/**",
                                "/docs/**").permitAll()
                        .anyRequest().authenticated()
                )

                // UsernamePasswordAuthenticationFilter보다 먼저 JWT 필터 실행
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
