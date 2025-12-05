package dev.syntax.global.core;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

	private final CoreApiProperties coreApiProperties;

	public ApiKeyAuthenticationFilter(CoreApiProperties properties) {
		this.coreApiProperties = properties;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		// 이 필터는 /channel/internal/** 로 시작하는 경로에만 적용됩니다.
		return !request.getRequestURI().startsWith("/channel/internal");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
		throws IOException, ServletException {

		String apiKey = req.getHeader("X-API-KEY");
		String expectedApiKey = coreApiProperties.getApiKey();

		// 1. API-KEY 검증
		if (expectedApiKey != null && expectedApiKey.equals(apiKey)) {

			// 2. 인증 객체 생성 (Core 서버 요청임을 나타냄)
			Collection<GrantedAuthority> authorities = Collections.singletonList(
				new SimpleGrantedAuthority("ROLE_CORE_SERVER"));
			Authentication authentication = new UsernamePasswordAuthenticationToken(
				"CORE_SERVER_PRINCIPAL",
				null,
				authorities
			);

			// 3. Security Context에 Authentication 객체를 저장합니다.
			SecurityContextHolder.getContext().setAuthentication(authentication);
			// -------------------------------------------------------------

			chain.doFilter(req, res);
		} else {
			// API-KEY가 없거나 일치하지 않으면 401 반환
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
		}
	}
}