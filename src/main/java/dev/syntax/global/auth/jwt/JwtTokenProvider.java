package dev.syntax.global.auth.jwt;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.auth.service.UserContextServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * JwtTokenProvider
 *
 * <p>JWT 토큰 생성·검증·인증 객체 변환을 담당하는 유틸리티 클래스입니다.
 * 인증 성공 시 UserContext 정보를 기반으로 토큰을 생성하며,
 * 클라이언트 요청에서 전달된 토큰을 검증한 뒤
 * SecurityContext에서 사용할 Authentication으로 복원합니다.
 *
 * <h2>주요 기능</h2>
 * <ul>
 *     <li>JWT 서명 키 초기화 및 만료 시간 관리</li>
 *     <li>Authentication → JWT 토큰 생성</li>
 *     <li>JWT 유효성 검증 및 예외 로그 처리</li>
 *     <li>JWT Claims 기반 UserContext 재구성</li>
 * </ul>
 *
 * @see UserContext
 * @see UserContextServiceImpl
 * @see io.jsonwebtoken.Jwts
 */
@Slf4j
@Component
public class JwtTokenProvider {

	private static final String AUTHORITIES_KEY = "auth";

	// yml에서 jwt.expiration-days 값을 가져와서 만료기간 계산
	private final long accessTokenExpireTime;
	private final Key key;
	private final UserContextServiceImpl userContextService;

	/**
	 * JwtTokenProvider 생성자.
	 *
	 * <p>application.yml에서 주입된 JWT 설정값을 사용하여
	 * 토큰 서명에 활용할 비밀 키를 초기화하고,
	 * 액세스 토큰의 만료 시간을 계산합니다.
	 *
	 * @param secretKey       Base64 인코딩된 JWT 서명용 비밀 키
	 * @param expirationDays  액세스 토큰 만료 기간(일 단위)
	 */
	public JwtTokenProvider(
		@Value("${jwt.secret}") String secretKey,
		@Value("${jwt.expiration-days}") long expirationDays,
		UserContextServiceImpl userContextService
	) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.accessTokenExpireTime = java.util.concurrent.TimeUnit.DAYS.toMillis(expirationDays);
		this.userContextService = userContextService;
	}

	/**
	 * 인증 정보를 기반으로 JWT 토큰을 생성합니다.
	 */
	public String generateToken(Authentication authentication) {
		UserContext userContext = (UserContext)authentication.getPrincipal();

		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		long now = System.currentTimeMillis();
		Date accessTokenExpiresIn = new Date(now + accessTokenExpireTime);

		return Jwts.builder()
			// subject: userId
			.setSubject(String.valueOf(userContext.getId()))
			.claim(AUTHORITIES_KEY, authorities)
			.claim("role", userContext.getRole())
			.claim("familyId", userContext.getFamilyId())
			.setExpiration(accessTokenExpiresIn)
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();
	}

	/**
	 * JWT 토큰의 유효성을 검증합니다.
	 */
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.info("잘못된 JWT 서명입니다.");
		} catch (ExpiredJwtException e) {
			log.info("만료된 JWT 토큰입니다.");
		} catch (UnsupportedJwtException e) {
			log.info("지원되지 않는 JWT 토큰입니다.");
		} catch (IllegalArgumentException e) {
			log.info("JWT 토큰이 잘못되었습니다.");
		}
		return false;
	}

	/**
	 * JWT 토큰으로부터 Authentication 생성
	 */
	public Authentication getAuthentication(String token) {
		Claims claims = parseClaims(token);

		Long userId = Long.parseLong(claims.getSubject());

		// DB에서 다시 조회하여 최신 UserContext 생성
		UserContext userContext = userContextService.loadUserById(userId);

		return new UsernamePasswordAuthenticationToken(
			userContext,
			null,
			userContext.getAuthorities()
		);
	}

	/**
	 * JWT 토큰에서 Claims를 파싱합니다.
	 * 만료된 토큰이라도 Claims를 반환하도록 처리합니다.
	 */
	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}
}