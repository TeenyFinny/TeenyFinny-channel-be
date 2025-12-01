package dev.syntax.domain.auth.service;

public interface OtpRateLimitService {
    
    /**
     * OTP 요청을 기록하고 제한을 확인합니다.
     * 제한 초과 시 BusinessException을 발생시킵니다.
     * 
     * @param userId 요청자
     */
	void validateAndRecordOtpRequest(Long userId);

}
