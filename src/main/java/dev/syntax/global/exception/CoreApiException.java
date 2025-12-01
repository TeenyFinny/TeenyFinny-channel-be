package dev.syntax.global.exception;

import dev.syntax.global.response.error.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

/**
 * Core 서버 API 호출 중 발생하는 예외 클래스입니다.
 * <p>
 * RestTemplate을 통한 Core 서버 통신 시 발생하는 HTTP 에러(4xx, 5xx)를
 * 애플리케이션 레벨의 예외로 변환합니다.
 * </p>
 */
@Getter
public class CoreApiException extends BusinessException {
	
	/** Core 서버의 HTTP 상태 코드 */
	private final HttpStatusCode httpStatusCode;
	
	/** Core 서버의 원본 에러 메시지 */
	private final String coreErrorMessage;
	
	/**
	 * Core API 예외를 생성합니다.
	 *
	 * @param errorCode Channel 서버의 에러 코드 (예: CORE_API_ERROR)
	 * @param httpStatusCode Core 서버로부터 받은 HTTP 상태 코드
	 * @param coreErrorMessage Core 서버로부터 받은 에러 메시지
	 */
	public CoreApiException(ErrorCode errorCode, HttpStatusCode httpStatusCode, String coreErrorMessage) {
		super(errorCode);
		this.httpStatusCode = httpStatusCode;
		this.coreErrorMessage = coreErrorMessage;
	}
}
