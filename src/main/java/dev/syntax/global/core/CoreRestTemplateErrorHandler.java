package dev.syntax.global.core;

import java.io.IOException;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.syntax.global.exception.CoreApiException;
import dev.syntax.global.response.BaseErrorResponse;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core 서버 API 호출 시 HTTP 에러를 처리하는 RestTemplate 에러 핸들러입니다.
 * <p>
 * 4xx, 5xx 에러 응답을 감지하고 {@link CoreApiException}으로 변환합니다.
 * Core 서버의 {@link BaseErrorResponse}를 파싱하여 상세 에러 메시지를 추출합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoreRestTemplateErrorHandler implements ResponseErrorHandler {

	private final ObjectMapper objectMapper;

	/**
	 * HTTP 응답이 에러인지 판단합니다.
	 *
	 * @param response HTTP 응답 객체
	 * @return 4xx, 5xx 에러인 경우 true
	 * @throws IOException 응답 읽기 실패 시
	 */
	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return response.getStatusCode().isError();
	}

	/**
	 * HTTP 에러 응답을 처리하고 CoreApiException을 발생시킵니다.
	 *
	 * @param response HTTP 응답 객체
	 * @throws IOException 응답 읽기 실패 시
	 * @throws CoreApiException Core API 에러 발생 시
	 */
	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		HttpStatusCode statusCode = response.getStatusCode();
		String errorMessage = "Core 서버 에러";

		try {
			// Core 서버의 에러 응답을 BaseErrorResponse로 파싱
			BaseErrorResponse<?> errorResponse = objectMapper.readValue(
				response.getBody(),
				BaseErrorResponse.class
			);
			errorMessage = errorResponse.getMessage();
		} catch (Exception e) {
			log.warn("Core Api 파싱 에러");
		}

		// HTTP 상태 코드에 따라 적절한 ErrorBaseCode 매핑
		ErrorBaseCode errorCode = mapStatusCodeToErrorCode(statusCode);

		throw new CoreApiException(errorCode, statusCode, errorMessage);
	}

	/**
	 * HTTP 상태 코드를 ErrorBaseCode로 매핑합니다.
	 *
	 * @param statusCode HTTP 상태 코드
	 * @return 매핑된 ErrorBaseCode
	 */
	private ErrorBaseCode mapStatusCodeToErrorCode(HttpStatusCode statusCode) {
		int code = statusCode.value();

		if (code == 401) {
			return ErrorBaseCode.CORE_API_UNAUTHORIZED;
		} else if (code == 403) {
			return ErrorBaseCode.CORE_API_FORBIDDEN;
		} else if (code == 404) {
			return ErrorBaseCode.CORE_API_NOT_FOUND;
		} else if (code == 408) {
			return ErrorBaseCode.CORE_API_TIMEOUT;
		} else if (code == 503) {
			return ErrorBaseCode.CORE_API_UNAVAILABLE;
		} else {
			return ErrorBaseCode.CORE_API_ERROR;
		}
	}
}
