package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.EmailValidationReq;
import dev.syntax.domain.auth.dto.FindEmailReq;
import dev.syntax.domain.auth.dto.FindEmailRes;
import dev.syntax.domain.auth.dto.IdentityVerifyReq;
import dev.syntax.domain.auth.dto.IdentityVerifyRes;
import dev.syntax.domain.auth.dto.PasswordVerifyReq;
import dev.syntax.domain.auth.dto.PasswordVerifyRes;
import dev.syntax.domain.auth.dto.RefreshTokenRes;
import dev.syntax.domain.auth.dto.ResetPasswordReq;
import dev.syntax.domain.auth.dto.SimplePasswordVerifyReq;
import dev.syntax.domain.auth.dto.UpdatePasswordReq;
import dev.syntax.domain.auth.dto.UpdatePushReq;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;

/**
 * 인증(Authentication)과 인가(Authorization)에 필요한
 * 보안 관련 도메인 로직을 제공하는 Service 인터페이스입니다.
 *
 * <p>토큰 및 비밀번호 검증 기능을 포함하며,
 * 회원가입 과정에서 필요한 이메일 중복 확인 기능도 제공합니다.
 */
public interface AuthService {

	/**
	 * 이메일 중복 여부를 검증합니다.
	 * <p>
	 * 전달된 이메일이 이미 사용자 테이블에 존재할 경우
	 * {@link BusinessException}을 발생시켜 가입을 차단합니다.
	 *
	 * @param request 이메일 중복 검증 요청 DTO
	 * @throws BusinessException 이메일이 이미 존재하는 경우 발생
	 */
	void checkEmailDuplicate(EmailValidationReq request);

	/**
	 * 사용자가 입력한 비밀번호가 현재 계정의 비밀번호와 일치하는지 검증합니다.
	 * <p>
	 * 비밀번호가 일치하지 않으면 {@link BusinessException}을 발생시키며,
	 * 일치하면 {@link PasswordVerifyRes} 객체를 반환합니다.
	 *
	 * @param userId 검증할 사용자의 ID
	 * @param request 사용자가 입력한 비밀번호를 담은 요청 DTO
	 * @return 비밀번호 일치 여부를 담은 {@link PasswordVerifyRes}
	 * @throws BusinessException 비밀번호가 일치하지 않는 경우 발생
	 */
	PasswordVerifyRes verifyPassword(Long userId, PasswordVerifyReq request);

	/**
	 * 사용자가 입력한 간편비밀번호가 현재 계정의 간편비밀번호와 일치하는지 검증합니다.
	 * <p>
	 * 간편비밀번호가 일치하지 않으면 {@link BusinessException}을 발생시키며,
	 * 일치하면 {@link PasswordVerifyRes} 객체를 반환합니다.
	 *
	 * @param userId 검증할 사용자의 ID
	 * @param request 사용자가 입력한 간편비밀번호 요청 DTO
	 * @return 간편비밀번호 일치 여부를 담은 {@link PasswordVerifyRes}
	 * @throws BusinessException 간편비밀번호가 일치하지 않을 경우 발생
	 */
	PasswordVerifyRes verifySimplePassword(Long userId, SimplePasswordVerifyReq request);

	/**
	 * 사용자의 비밀번호를 변경합니다.
	 * <p>
	 * 현재 비밀번호를 검증한 후 새 비밀번호로 변경합니다.
	 * </p>
	 *
	 * @param user 현재 인증된 사용자 정보
	 * @param request 현재 비밀번호와 새 비밀번호
	 * @throws dev.syntax.global.exception.BusinessException 현재 비밀번호가 일치하지 않을 경우
	 */
	void updatePassword(UserContext user, UpdatePasswordReq request);

	/**
	 * 사용자의 간편 비밀번호를 변경합니다.
	 * <p>
	 * 6자리 숫자로 구성된 간편 비밀번호를 변경합니다.
	 * </p>
	 *
	 * @param user 현재 인증된 사용자 정보
	 * @param request 새로운 간편 비밀번호 (6자리 숫자)
	 */
	void updateSimplePassword(UserContext user, SimplePasswordVerifyReq request);

	/**
	 * 사용자의 푸시 알림 설정을 변경합니다.
	 * <p>
	 * 푸시 알림과 야간 푸시 알림 설정을 변경합니다.
	 * 요청 DTO에 포함된 필드만 업데이트되며, null인 필드는 기존 값을 유지합니다.
	 * </p>
	 *
	 * @param user 현재 인증된 사용자 정보
	 * @param request 푸시 알림 설정 (pushEnabled, nightPushEnabled)
	 */
	void updatePushSettings(UserContext user, UpdatePushReq request);

	/**
	 * 사용자의 최신 정보로 새로운 JWT 토큰을 발급합니다.
	 *
	 * @param userId 사용자 ID
	 * @return 새로운 JWT 토큰
	 */
	RefreshTokenRes refreshToken(Long userId);

	/**
	 * 본인인증 Mock을 수행합니다.
	 *
	 * @param request 본인인증 요청 DTO
	 * @return 인증 결과
	 */
	IdentityVerifyRes verifyIdentityMock(IdentityVerifyReq request);

	/**
	 * 전화번호, 생년월일, 이름으로 이메일을 찾습니다.
	 * <p>
	 * 일치하는 사용자가 없으면 {@link BusinessException}을 발생시킵니다.
	 *
	 * @param request ID 찾기 요청 DTO
	 * @return 찾은 이메일 주소
	 * @throws BusinessException 일치하는 사용자가 없을 경우 발생
	 */
	FindEmailRes findEmail(FindEmailReq request);

	/**
	 * 비밀번호 재설정 링크를 이메일로 발송합니다.
	 * <p>
	 * 이메일, 전화번호, 생년월일, 이름이 모두 일치하는 사용자가 있는지 확인하고,
	 * 일치하면 비밀번호 재설정 링크를 이메일로 발송합니다.
	 * 일치하는 사용자가 없으면 {@link BusinessException}을 발생시킵니다.
	 *
	 * @param request 비밀번호 재설정 요청 DTO
	 * @throws BusinessException 일치하는 사용자가 없을 경우 발생
	 */
	void resetPassword(ResetPasswordReq request);
}

