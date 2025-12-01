package dev.syntax.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.auth.entity.OtpRateLimit;
import dev.syntax.domain.auth.repository.OtpRateLimitRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpRateLimitServiceImpl implements OtpRateLimitService {

	private final OtpRateLimitRepository repository;

	// 1분 제한: 3회
	private static final int LIMIT_1_MIN = 3;
	// 10분 제한: 5회
	private static final int LIMIT_10_MIN = 5;

	@Override
	@Transactional
	public void validateAndRecordOtpRequest(Long userId) {
		LocalDateTime now = LocalDateTime.now();

		// 요청을 먼저 기록하고 검사하는 'act-then-check' 패턴을 사용하여 동시성 문제를 완화합니다.
		// 제한을 초과하면 트랜잭션이 롤백되어 기록이 취소됩니다.
		repository.save(
			OtpRateLimit.builder()
				.userId(userId)
				.requestedAt(now)
				.build()
		);

		long count10Min = repository.countRecentRequests(
			userId,
			now.minusMinutes(10)
		);

		if (count10Min > LIMIT_10_MIN) {
			throw new BusinessException(ErrorBaseCode.OTP_TOO_MANY_REQUESTS);
		}

		long count1Min = repository.countRecentRequests(
			userId,
			now.minusMinutes(1)
		);

		if (count1Min > LIMIT_1_MIN) {
			throw new BusinessException(ErrorBaseCode.OTP_TOO_MANY_REQUESTS);
		}
	}
}
