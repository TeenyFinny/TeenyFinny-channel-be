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
     * 401 UNAUTHORIZED - 리소스 접근 권한
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", "AUTH01");

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