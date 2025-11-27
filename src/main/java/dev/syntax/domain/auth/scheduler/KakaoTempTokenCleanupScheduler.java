package dev.syntax.domain.auth.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.auth.repository.KakaoTempTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카카오 임시 토큰 정리 스케줄러
 * 
 * 만료된 임시 토큰을 주기적으로 삭제하여 DB 용량을 관리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTempTokenCleanupScheduler {

	private final KakaoTempTokenRepository kakaoTempTokenRepository;

	/**
	 * 매 10분마다 만료된 토큰 삭제
	 */
	@Scheduled(cron = "0 */10 * * * *")
	@Transactional
	public void cleanupExpiredTokens() {
		try {
			kakaoTempTokenRepository.deleteExpiredTokens(LocalDateTime.now());
			log.info("[카카오 임시 토큰 정리] 만료된 토큰 삭제 완료");
		} catch (Exception e) {
			log.error("[카카오 임시 토큰 정리 실패] error: {}", e.getMessage());
		}
	}
}
