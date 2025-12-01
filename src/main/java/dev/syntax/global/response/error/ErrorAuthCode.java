package dev.syntax.global.response.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 인증/인가(Auth) 관련 오류 코드를 관리하는 열거형입니다.
 * <p>
 * {@code errorCode} 필드로 서비스 내부 식별용 코드를 함께 제공하여
 * 클라이언트/로그 수집/관제에서 정밀한 분류가 가능하도록 합니다.
 * </p>
 *
 * <h3>설계 가이드</h3>
 * <ul>
 *   <li><b>HTTP 상태</b>: 인증 실패는 주로 {@code 401 UNAUTHORIZED}, 권한 부족은 {@code 403 FORBIDDEN}을 사용합니다.</li>
 *   <li><b>errorCode 규칙</b>: 접두사 {@code AUTH} + 2자리 숫자(예: AUTH01). 팀 컨벤션에 맞춰 일관되게 관리하세요.</li>
 * </ul>
 *
 * <h3>사용 예</h3>
 * <pre>{@code
 * if (!tokenProvider.validate(accessToken)) {
 *     throw new BusinessException(ErrorAuthCode.INVALID_TOKEN);
 * }
 * }</pre>
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorAuthCode implements ErrorBaseCodeForErrorCode {

    /**
     * 400 BAD_REQUEST - 잘못된 요청
     */
    INVALID_IDENTITY_FORMAT(HttpStatus.BAD_REQUEST, "본인 인증 요청 형식이 올바르지 않습니다.", "AUTH06"),
	INVALID_FORMAT(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.", "AUTH07"),
    /**
	 * 401 UNAUTHORIZED - 인증 실패
	 */
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", "AUTH01"),
	FAMILY_OTP_MISMATCH(HttpStatus.UNAUTHORIZED, "OTP 코드를 확인해주세요.", "FAM01"),
	FAMILY_OTP_TIMEOUT(HttpStatus.UNAUTHORIZED, "OTP 코드가 만료되었습니다.", "FAM02"),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "패스워드를 확인해주세요.", "AUTH04"),
    SIMPLE_PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "간편비밀번호가 일치하지 않습니다.", "AUTH05"),
	TOKEN_VALIDATION_FAILED(HttpStatus.UNAUTHORIZED, "카카오 인증이 실패했습니다.", "KAKAO01"),

     /**
	 * 403 FORBIDDEN - 권한 부족
	 */
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", "AUTH02"),

	/**
	 * 409 CONFLICT - 메일 중복 (auth에서 사용)
	 */
	EMAIL_CONFLICT(HttpStatus.CONFLICT, "해당 이메일로 가입할 수 없습니다.", "AUTH03"),
	KAKAO_EMAIL_CONFLICT(HttpStatus.CONFLICT, "해당 이메일로 가입된 카카오 계정이 있습니다.", "KAKAO02"),
	KAKAO_PROVIDER_CONFLICT(HttpStatus.CONFLICT, "이미 등록된 회원입니다.", "KAKAO03"),
	CORE_INIT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "회원가입 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", "AUTH07");

	// 마지막 항목의 ;을 쉼표로 바꾸고 여기에 마저 추가

	private final HttpStatus httpStatus;
	private final String message;
	private final String errorCode;

	@Override
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	@Override
	public String getErrorCode() {
		return errorCode;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
