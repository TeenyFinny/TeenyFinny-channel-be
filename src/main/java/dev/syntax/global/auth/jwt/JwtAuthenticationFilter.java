package dev.syntax.global.auth.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 클라이언트의 요청을 가로채서 JWT 토큰을 검증하는 필터입니다.
 * OncePerRequestFilter를 상속받아 요청당 한 번만 실행됩니다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * 필터링 로직을 수행합니다.
	 * 요청 헤더에서 토큰을 추출하고, 유효한 토큰인 경우 인증 정보를 SecurityContext에 저장합니다.
	 *
	 * @param request     HttpServletRequest
	 * @param response    HttpServletResponse
	 * @param filterChain FilterChain
	 * @throws ServletException 서블릿 예외
	 * @throws IOException      입출력 예외
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String path = request.getRequestURI();

		// SSE 구독 요청은 JWT 실패 시 Security 계층으로 넘기지 않고 여기서 바로 처리
		if (path.equals("/notifications/subscribe")) {
			String token = resolveToken(request);
			if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
				response.setStatus(401);
				response.getOutputStream().close();  // 스트림 종료
				return; // Chain 더 내려가지 않음
			}
		}

		String token = resolveToken(request);

		if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
			Authentication authentication = jwtTokenProvider.getAuthentication(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * JWT 필터를 건너뛸 경로를 설정합니다.
	 * /internal/** 경로는 Core 서버에서 API-KEY로 인증하는 경로이므로 JWT 검증을 건너뜁니다.
	 *
	 * @param request HttpServletRequest
	 * @return 필터를 건너뛸지 여부
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/channel/internal")
			|| path.equals("/notifications/subscribe");
	}

	/**
	 * 요청 헤더에서 Bearer 토큰을 추출합니다.
	 *
	 * @param request HttpServletRequest
	 * @return 추출된 토큰 문자열, 없으면 null
	 */
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
			return bearerToken.substring(BEARER_PREFIX.length());
		}
		return null;
	}
}
