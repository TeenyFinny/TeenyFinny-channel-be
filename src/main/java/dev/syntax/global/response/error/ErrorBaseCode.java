package dev.syntax.global.response.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 인증/인가(Auth)를 제외한 일반 도메인/플랫폼 영역의 오류 코드를 관리하는 열거형입니다.
 * <p>
 * {@link ErrorCode}를 구현하여 각 항목별 HTTP 상태와 사용자 메시지를 제공합니다.
 * 전역 예외 처리기(예: {@code @RestControllerAdvice})에서 이 enum을 사용해
 * 일관된 오류 응답을 생성하는 데 활용합니다.
 * </p>
 *
 * <h3>사용 예</h3>
 * <pre>{@code
 * // 서비스/도메인 로직에서
 * throw new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY);
 *
 * // 전역 예외 처리기에서
 * @ExceptionHandler(BusinessException.class)
 * public ResponseEntity<BaseResponse<?>> handle(BusinessException e) {
 *     ErrorCode code = e.getCode();
 *     return ResponseEntity
 *         .status(code.getHttpStatus())
 *         .body(ApiResponseUtil.failure(code));
 * }
 * }</pre>
 *
 * @see ErrorCode
 * @see org.springframework.http.HttpStatus
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorBaseCode implements ErrorCode {

    /**
     * 400 BAD_REQUEST - 잘못된 요청
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    /**
     * 401 UNAUTHORIZED - 리소스 접근 권한
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

    /**
     * 403 FORBIDDEN - 리소스 접근 금지
     */
    FORBIDDEN(HttpStatus.FORBIDDEN, "리소스 접근이 거부되었습니다."),

    /**
     * 404 NOT FOUND - 찾을 수 없음
     */
    NOT_FOUND_ENTITY(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),

    /**
     * 405 METHOD NOT ALLOWED - 허용되지 않은 메서드
     */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,  "잘못된 HTTP METHOD 요청입니다."),

    /**
     * 409 CONFLICT
     */
    CONFLICT(HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),

    /**
     * 410 GONE
     */
    GONE(HttpStatus.GONE, "더 이상 사용되지 않는 리소스입니다."),

    /**
     * 413 PAYLOAD_TOO_LARGE
     */
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "요청 데이터 크기가 너무 큽니다."),

    /**
     * 415 UNSUPPORTED_MEDIA_TYPE
     */
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원되지 않는 데이터 형식입니다."),

    /**
     * 429 TOO_MANY_REQUESTS
     */
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "너무 많은 요청입니다."),

    /**
     * 500 INTERNAL SERVER ERROR
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),

    /**
     * 501 NOT IMPLEMENTED
     */
    NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "NOT_IMPLEMENTED"),

    /**
     * 502 BAD_GATEWAY
     */
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "BAD GATEWAY."),

    /**
     * 503 SERVICE_UNAVAILABLE
     */
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE"),


    /**
     * 504 GATEWAY_TIMEOUT
     */
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "GATEWAY_TIMEOUT"),

    /**
     * 504 GATEWAY_TIMEOUT
     */
    HTTP_VERSION_NOT_SUPPORTED(HttpStatus.HTTP_VERSION_NOT_SUPPORTED, "HTTP_VERSION_NOT_SUPPORTED");



    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}