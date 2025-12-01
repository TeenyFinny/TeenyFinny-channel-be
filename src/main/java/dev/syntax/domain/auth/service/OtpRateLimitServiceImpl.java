package dev.syntax.domain.auth.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

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
	public void validateAndRecordOtpRequest(Long userId) {

		long count1Min = repository.countRecentRequests(
			userId,
			LocalDateTime.now().minusMinutes(1)
		);

		if (count1Min >= LIMIT_1_MIN) {
			throw new BusinessException(ErrorBaseCode.OTP_TOO_MANY_REQUESTS);
		}

		long count10Min = repository.countRecentRequests(
			userId,
			LocalDateTime.now().minusMinutes(10)
		);

		if (count10Min >= LIMIT_10_MIN) {
			throw new BusinessException(ErrorBaseCode.OTP_TOO_MANY_REQUESTS);
		}

		// 허용 → insert
		repository.save(
			OtpRateLimit.builder()
				.userId(userId)
				.requestedAt(LocalDateTime.now())
				.build()
		);
	}
}
