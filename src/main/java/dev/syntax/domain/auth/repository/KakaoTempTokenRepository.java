package dev.syntax.domain.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import dev.syntax.domain.auth.entity.KakaoTempToken;

/**
 * 카카오 임시 토큰 Repository
 */
public interface KakaoTempTokenRepository extends JpaRepository<KakaoTempToken, String> {

	/**
	 * 토큰으로 조회
	 */
	Optional<KakaoTempToken> findByToken(String token);

	/**
	 * 만료된 토큰 삭제
	 */
	@Modifying
	@Query("DELETE FROM KakaoTempToken t WHERE t.expiresAt < :now")
	void deleteExpiredTokens(@Param("now") LocalDateTime now);

	/**
	 * providerId로 기존 토큰 삭제 (중복 방지)
	 */
	@Modifying
	@Query("DELETE FROM KakaoTempToken t WHERE t.providerId = :providerId")
	void deleteByProviderId(@Param("providerId") String providerId);
}
