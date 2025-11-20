package dev.syntax.global.jwt;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

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
 * JWT 토큰 생성 및 검증을 담당하는 유틸리티 클래스입니다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

	private static final String AUTHORITIES_KEY = "auth";

	// yml에서 jwt.expiration-days 값을 가져와서 만료기간 계산
	private final long accessTokenExpireTime;

	private final Key key;

	/**
	 * JwtTokenProvider 생성자.
	 * application.yml에서 설정한 secret key를 사용하여 key를 초기화합니다.
	 *
	 * @param secretKey JWT 서명에 사용할 비밀 키
	 * @param expirationDays yml에서 주입된 만료기간(일 단위)
	 */
	public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
		@Value("${jwt.expiration-days}") long expirationDays) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);

		// days → ms 변환
		this.accessTokenExpireTime = expirationDays * 24 * 60 * 60 * 1000;
	}

	/**
	 * 인증 정보를 기반으로 JWT 토큰을 생성합니다.
	 *
	 * @param authentication 인증 정보
	 * @return 생성된 JWT 토큰
	 */
	public String generateToken(Authentication authentication) {
		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining(","));

		long now = (new Date()).getTime();
		Date accessTokenExpiresIn = new Date(now + accessTokenExpireTime);

		// TODO(다음 PR에서 구현 예정): Authentication 객체에서 userId, role 꺼내기 추가

		return Jwts.builder()
			.setSubject(authentication.getName())
			.claim(AUTHORITIES_KEY, authorities)
			.setExpiration(accessTokenExpiresIn)
			.signWith(key, SignatureAlgorithm.HS512)
			.compact();
	}

	/**
	 * JWT 토큰의 유효성을 검증합니다.
	 *
	 * @param token 검증할 JWT 토큰
	 * @return 유효한 토큰이면 true, 그렇지 않으면 false
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
	 * JWT 토큰에서 Claims를 파싱합니다.
	 * 만료된 토큰이라도 Claims를 반환하도록 처리합니다.
	 *
	 * @param accessToken JWT 토큰
	 * @return Claims 객체
	 */
	@SuppressWarnings("java:S1144")
	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}
}
