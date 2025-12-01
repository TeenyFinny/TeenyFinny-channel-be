package dev.syntax.global.response.error;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
	TX_INVALID_PERIOD(HttpStatus.BAD_REQUEST,
		"요청한 조회 기간이 유효하지 않습니다. 과거 또는 현재 월까지만 조회할 수 있습니다."),
	TX_INVALID_TRANSACTION_ID(HttpStatus.BAD_REQUEST,
		"요청한 거래 ID 형식이 올바르지 않습니다."),
	GOAL_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "목표 금액/납입 금액이 유효하지 않습니다."),
	GOAL_INVALID_PAYDAY(HttpStatus.BAD_REQUEST, "납입일이 유효하지 않습니다."),
	GOAL_ALREADY_PENDING(HttpStatus.BAD_REQUEST, "이미 승인 대기 중인 목표가 있어요."),
	GOAL_ALREADY_ONGOING(HttpStatus.BAD_REQUEST, "이미 진행 중인 목표가 있어요."),
	GOAL_NOT_ONGOING(HttpStatus.BAD_REQUEST, "진행 중인 목표가 아닙니다."),
	GOAL_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "아직 목표에 도달하지 않았어요."),
    GOAL_IS_COMPLETED(HttpStatus.BAD_REQUEST, "이미 달성한 목표예요!"),
    INVEST_ACCOUNT_REQUIRED(HttpStatus.BAD_REQUEST, "투자 계좌가 없어 투자 이체를 설정할 수 없습니다."),
	INVALID_RATIO_VALUE(HttpStatus.BAD_REQUEST, "비율 값이 잘못되었습니다."),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
	INVALID_ACCOUNT_TYPE(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
	REPORT_NOT_AVAILABLE_YET(HttpStatus.BAD_REQUEST, "리포트는 전월까지만 조회할 수 있습니다."),

	/**
	 * 401 UNAUTHORIZED - 리소스 접근 권한
	 */
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),

	/**
	 * 403 FORBIDDEN - 리소스 접근 금지
	 */
	FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	TX_NO_PERMISSION(HttpStatus.FORBIDDEN, "해당 계좌에 대한 접근 권한이 없습니다."),
	GOAL_REQUEST_FORBIDDEN(HttpStatus.FORBIDDEN, "부모는 목표에 관해 요청할 수 없습니다."),
	GOAL_CHILD_NOT_MATCH(HttpStatus.FORBIDDEN, "해당 목표는 당신의 자녀가 생성한 목표가 아닙니다."),
	GOAL_ACCESS_FORBIDDEN(HttpStatus.FORBIDDEN, "목표 확정 및 취소는 부모만 가능합니다."),
	PARENT_ONLY_FEATURE(HttpStatus.FORBIDDEN, "부모만 사용할 수 있는 기능입니다."),
	INVALID_CHILD(HttpStatus.FORBIDDEN, "해당 자녀에 대한 접근 권한이 없습니다."),

	/**
	 * 404 NOT FOUND - 찾을 수 없음
	 */
	NOT_FOUND_ENTITY(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
	QUIZ_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
	QUIZ_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
	GOAL_NOT_FOUND(HttpStatus.NOT_FOUND, "목표 정보를 찾을 수 없습니다."),
	GOAL_PARENT_NOT_FOUND(HttpStatus.NOT_FOUND, "가족 등록을 먼저 진행해 주세요."),
	TX_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 계좌를 찾을 수 없습니다."),
	TX_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 거래내역을 찾을 수 없습니다."),
	ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 계좌를 찾을 수 없습니다."),
	CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 카드를 찾을 수 없습니다."),
	AUTO_TRANSFER_NOT_FOUND(HttpStatus.NOT_FOUND, "자동이체를 찾을 수 없습니다."),

	/**
	 * 405 METHOD NOT ALLOWED - 허용되지 않은 메서드
	 */
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "잘못된 HTTP METHOD 요청입니다."),

	/**
	 * 409 CONFLICT
	 */
	CONFLICT(HttpStatus.CONFLICT, "이미 존재합니다."),
	GOAL_ALREADY_DECIDED(HttpStatus.CONFLICT, "이미 승인 또는 거절된 목표입니다."),
	GOAL_CANCEL_ALREADY_REQUESTED(HttpStatus.CONFLICT, "이미 목표 중도 해지 요청을 보냈습니다."),
	CARD_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 카드입니다."),
	DUPLICATE_REPORT(HttpStatus.CONFLICT, "해당 월의 리포트가 이미 존재합니다."),
	AUTO_TRANSFER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 자동이체가 설정되어 있습니다."),
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
	OTP_TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "OTP 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),


	/**
	 * 500 INTERNAL SERVER ERROR
	 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "계좌 생성에 실패했습니다."),
	USER_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "계정 생성에 실패했습니다."),
	AUTO_TRANSFER_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "자동이체 생성에 실패했습니다."),
	AUTO_TRANSFER_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "자동이체 수정에 실패했습니다."),
	AUTO_TRANSFER_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "자동이체 삭제에 실패했습니다."),
	SSE_CONNECT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "초기 SSE 연결 이벤트 전송에 실패했습니다."),

	/**
	 * 카카오 로그인 관련 에러
	 */
	TOKEN_ISSUE_FAILED(HttpStatus.BAD_GATEWAY, "카카오 토큰 발급 실패"),
	USER_INFO_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "카카오 사용자 정보 조회 실패"),
	USER_INFO_PARSE_FAILED(HttpStatus.BAD_GATEWAY,"카카오 사용자 정보 파싱 실패"),

	/**
	 * Core API 관련 에러
	 */
	CORE_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Core 서버와의 통신 중 오류가 발생했습니다."),
	CORE_API_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Core 서버 인증에 실패했습니다."),
	CORE_API_FORBIDDEN(HttpStatus.FORBIDDEN, "Core 서버 접근 권한이 없습니다."),
	CORE_API_NOT_FOUND(HttpStatus.NOT_FOUND, "Core 서버에서 요청한 리소스를 찾을 수 없습니다."),
	CORE_API_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "Core 서버 응답 시간이 초과되었습니다."),
	CORE_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Core 서버를 사용할 수 없습니다."),

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