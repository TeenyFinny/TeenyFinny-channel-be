package dev.syntax.global.exception;

import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import dev.syntax.global.response.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;

/**
 * 전역 예외 처리를 담당하는 핸들러입니다.
 * <p>
 * 처리 대상 예외 종류:
 * - CustomBaseException: 컨트롤러·프레젠테이션 계층에서 발생하는 예외
 * - BusinessException: 서비스·도메인 계층에서 발생하는 비즈니스 규칙 예외
 * - AuthenticationException: 인증 실패 시 발생하는 스프링 시큐리티 예외
 * - AccessDeniedException: 인가 실패(권한 부족) 예외
 * - Exception: 위에 포함되지 않는 모든 예외(서버 오류)
 *
 * 모든 예외는 ApiResponseUtil을 통해 BaseResponse 형태로 변환되며,
 * HTTP 상태 코드와 메시지는 ErrorCode 계열(enum)에서 정의한 값으로 매핑됩니다.
 * </p>
 * SuppressWarnings("java:S1452") : DTO 타입이 다양한 API 응답은 ResponseEntity<?> 혹은 ApiResponse<?> 사용을 허용한다
 */
@SuppressWarnings("java:S1452")
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * CustomBaseException 처리.
	 * 컨트롤러 또는 요청 처리 과정에서 발생한 커스텀 예외를 변환합니다.
	 * ErrorAuthCode인 경우 AuthErrorResponse를, 그 외에는 BaseErrorResponse를 반환합니다.
	 */
	@ExceptionHandler(CustomBaseException.class)
	public ResponseEntity<BaseResponse<?>> handleCustomException(CustomBaseException e) {
		log.error("[CustomException] {}: {}", e.getErrorCode(), e.getMessage());
		return createErrorResponse(e.getErrorCode());
	}

	/**
	 * BusinessException 처리.
	 * 서비스·도메인 계층의 비즈니스 규칙 위반 예외를 처리합니다.
	 * ErrorAuthCode인 경우 AuthErrorResponse를, 그 외에는 BaseErrorResponse를 반환합니다.
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<BaseResponse<?>> handleBusinessException(BusinessException e) {
		log.error("[BusinessException] {} : {}", e.getErrorCode(), e.getMessage());
		return createErrorResponse(e.getErrorCode());
	}

	/**
	 * CoreApiException 처리.
	 * Core 서버 API 호출 중 발생한 예외를 처리하고 일관된 에러 응답을 반환합니다.
	 */
	@ExceptionHandler(CoreApiException.class)
	public ResponseEntity<BaseResponse<?>> handleCoreApiException(CoreApiException e) {
		// 현재 인증된 사용자 정보 추출
		Long userId = getCurrentUserId();

		log.error("[{}] : Core Status={}, Core Message={}, User Id={}",
			e.getErrorCode(), e.getHttpStatusCode(), e.getCoreErrorMessage(), userId);
		return createErrorResponse(e.getErrorCode());
	}

	/**
	 * ResourceAccessException 처리.
	 * Core 서버 연결 실패 시 발생하는 예외를 처리합니다.
	 */
	@ExceptionHandler(ResourceAccessException.class)
	public ResponseEntity<BaseResponse<?>> handleResourceAccessException(ResourceAccessException e) {
		// 현재 인증된 사용자 정보 추출
		Long userId = getCurrentUserId();

		log.error("[Core 서버 연결 실패] Message={}, User Id={}",
			e.getMessage(), userId);
		return ApiResponseUtil.failure(ErrorBaseCode.CORE_API_UNAVAILABLE);
	}

	private ResponseEntity<BaseResponse<?>> createErrorResponse(ErrorCode errorCode) {
		if (errorCode instanceof ErrorAuthCode) {
			return ApiResponseUtil.failure((ErrorAuthCode)errorCode);
		}
		return ApiResponseUtil.failure(errorCode);
	}

	/**
	 * AuthenticationException 처리 (401 Unauthorized)
	 * <p>
	 * Spring Security에서 인증 실패 시 발생하는 예외를 처리합니다.
	 * </p>
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<BaseResponse<?>> handleAuthenticationException(AuthenticationException e) {
		log.error("[AuthenticationException] {}", e.getMessage());
		return ApiResponseUtil.failure(ErrorAuthCode.UNAUTHORIZED);
	}

	/**
	 * @Valid / @Validated 실패 시 발생
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<BaseResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {

		String errorMessage = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse("잘못된 요청입니다.");

		throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
	}

	/**
	 * AccessDeniedException 처리 (403 Forbidden)
	 * <p>
	 * Spring Security에서 권한 부족 시 발생하는 예외를 처리합니다.
	 * </p>
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<BaseResponse<?>> handleAccessDeniedException(AccessDeniedException e) {
		log.error("[AccessDeniedException] {}", e.getMessage());
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
		log.error("[Exception] {}", e.getMessage(), e);
		return ApiResponseUtil.failure(ErrorBaseCode.INTERNAL_SERVER_ERROR);
	}

	/**
	 * SSE 예외처리
	 * @param e
	 */
	@ExceptionHandler(AsyncRequestNotUsableException.class)
	public void handleSseClosing(AsyncRequestNotUsableException e) {
		log.debug("[SSE 종료] 클라이언트가 연결을 닫았습니다: {}", e.getMessage());
	}

	@ExceptionHandler(IOException.class)
	public void handleSseIOException(IOException e) {
		log.debug("[SSE 전송 중단] {}", e.getMessage());
	}



	private Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserContext userContext) {
			return userContext.getId();
		}
		return null;
	}
}
