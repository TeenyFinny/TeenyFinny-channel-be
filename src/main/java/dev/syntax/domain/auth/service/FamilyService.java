package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.OtpGenerateRes;
import dev.syntax.domain.auth.dto.OtpVerifyReq;
import dev.syntax.domain.auth.dto.OtpVerifyRes;

/**
 * 가족 관계 관리 비즈니스 로직을 정의하는 인터페이스입니다.
 */
public interface FamilyService {

    /**
     * 부모 사용자를 위한 OTP를 생성합니다.
     *
     * @param userId 부모 사용자 ID
     * @return OTP 생성 응답
     */
    OtpGenerateRes generateOtp(Long userId);

    /**
     * 자녀가 입력한 OTP를 검증하고 UserRelationship의 child를 업데이트합니다.
     * OTP는 생성 후 1분 이내에만 유효합니다.
     *
     * @param userId 자녀 사용자 ID
     * @param request OTP 검증 요청
     * @return OTP 검증 응답
     */
    OtpVerifyRes verifyOtpAndCreateRelationship(Long userId, OtpVerifyReq request);
}
