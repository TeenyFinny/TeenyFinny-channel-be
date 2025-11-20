package dev.syntax.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리를 담당하는 핸들러입니다.
 * <p>
 * 애플리케이션 전역에서 발생하는 예외를 일관된 형식으로 처리하여
 * 클라이언트에게 표준화된 에러 응답을 제공합니다.
 * </p>
 * @SuppressWarnings("java:S1452") : DTO 타입이 다양한 API 응답은 ResponseEntity<?> 혹은 ApiResponse<?> 사용을 허용한다
 */
@SuppressWarnings("java:S1452")
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * CustomBaseException 처리
	 * <p>
	 * 도메인 로직에서 발생하는 비즈니스 예외를 처리합니다.
	 * </p>
	 */
	@ExceptionHandler(CustomBaseException.class)
	public ResponseEntity<BaseResponse<?>> handleCustomException(CustomBaseException e) {
		log.error("CustomException: {}", e.getMessage());
		return ApiResponseUtil.failure(e.getErrorCode());
	}

	/**
	 * AuthenticationException 처리 (401 Unauthorized)
	 * <p>
	 * Spring Security에서 인증 실패 시 발생하는 예외를 처리합니다.
	 * </p>
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<BaseResponse<?>> handleAuthenticationException(AuthenticationException e) {
		log.error("AuthenticationException: {}", e.getMessage());
		return ApiResponseUtil.failure(ErrorAuthCode.UNAUTHORIZED);
	}

	/**
	 * AccessDeniedException 처리 (403 Forbidden)
	 * <p>
	 * Spring Security에서 권한 부족 시 발생하는 예외를 처리합니다.
	 * </p>
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<BaseResponse<?>> handleAccessDeniedException(AccessDeniedException e) {
		log.error("AccessDeniedException: {}", e.getMessage());
		return ApiResponseUtil.failure(ErrorAuthCode.ACCESS_DENIED);
	}

	/**
	 * 그 외 모든 예외 처리 (500 Internal Server Error)
	 * <p>
	 * 예상하지 못한 예외가 발생했을 때 처리합니다.
	 * </p>
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<BaseResponse<?>> handleException(Exception e) {
		log.error("Exception: {}", e.getMessage(), e);
		return ApiResponseUtil.failure(ErrorBaseCode.INTERNAL_SERVER_ERROR);
	}
}
