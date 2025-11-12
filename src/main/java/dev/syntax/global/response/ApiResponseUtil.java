package dev.syntax.global.response;

import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorCode;
import org.springframework.http.ResponseEntity;

/**
 * BaseResponse를 더 간결하게 생성·반환하기 위한 도우미 유틸리티입니다.
 * <p>
 * 컨트롤러에서 상태 코드와 표준화된 바디를 일관되게 리턴할 수 있도록
 * {@link ResponseEntity}와 {@link BaseResponse} 조합을 캡슐화합니다.
 * </p>
 */
public class ApiResponseUtil {
    /**
     * 데이터 없이 성공 메시지만 포함한 응답을 생성합니다.
     *
     * <h4>응답 예시</h4>
     * <pre>{@code
     * HTTP/1.1 200 OK
     * Content-Type: application/json
     *
     * {
     *   "message": "요청이 성공했습니다.",
     *   "errorCode": null,
     *   "data": null
     * }
     * }</pre>
     *
     * @param successCode 성공 코드를 나타내는 열거형
     * @return HTTP 상태는 {@code successCode.getHttpStatus()}이며, 바디는 메시지만 포함한 {@link BaseResponse}
     */
    public static ResponseEntity<BaseResponse<?>> success(final SuccessCode successCode) {
        return ResponseEntity.status(successCode.getHttpStatus())
                .body(BaseResponse.of(successCode));
    }

    /**
     * 성공 메시지와 함께 임의의 데이터 페이로드를 포함한 응답을 생성합니다.
     *
     * <h4>응답 예시</h4>
     * <pre>{@code
     * HTTP/1.1 201 Created
     * Content-Type: application/json
     *
     * {
     *   "message": "요청이 성공했습니다.",
     *   "errorCode": null,
     *   "data": {
     *     "id": 123,
     *     "name": "Alice",
     *     "price": "230,010,000"
     *   }
     * }
     * }</pre>
     *
     * @param successCode 성공 코드를 나타내는 열거형
     * @param data        응답 본문에 포함할 데이터
     * @param <T>         데이터 타입
     * @return HTTP 상태는 {@code successCode.getHttpStatus()}이며, 바디는 메시지와 {@code data}를 포함한 {@link BaseResponse}
     */
    public static <T> ResponseEntity<BaseResponse<?>> success(final SuccessCode successCode, final T data) {
        return ResponseEntity.status(successCode.getHttpStatus())
                .body(BaseResponse.of(successCode, data));
    }

    /**
     * 일반(ErrorAuth 제외) 오류 코드를 기반으로 오류 응답을 생성합니다.
     * <p>
     * 바디에는 오류 메시지가 포함되며, 별도 내부 {@code errorCode} 문자열이 없다면
     * {@link BaseResponse#getErrorCode()}는 {@code null}일 수 있습니다.
     * </p>
     *
     * <h4>응답 예시</h4>
     * <pre>{@code
     * HTTP/1.1 404 Not Found
     * Content-Type: application/json
     *
     * {
     *   "message": "대상을 찾을 수 없습니다.",
     *   "errorCode": null,
     *   "data": null
     * }
     * }</pre>
     *
     * @param errorBaseCode 인증/인가 영역을 제외한 오류 코드를 나타내는 열거형
     * @return HTTP 상태는 {@code errorBaseCode.getHttpStatus()}이며, 바디는 오류 메시지를 포함한 {@link BaseResponse}
     */
    public static ResponseEntity<BaseResponse<?>> failure(final ErrorCode errorBaseCode) {
        return ResponseEntity.status(errorBaseCode.getHttpStatus())
                .body(BaseResponse.of(errorBaseCode));
    }

    /**
     * 인증/인가(Auth) 관련 오류 코드를 기반으로 오류 응답을 생성합니다.
     * <p>
     * 바디에는 사용자 메시지와 내부 식별용 {@code errorCode} 문자열(예: {@code AUTH01})이 함께 들어갑니다.
     * </p>
     *
     * <h4>응답 예시</h4>
     * <pre>{@code
     * HTTP/1.1 401 Unauthorized
     * Content-Type: application/json
     *
     * {
     *   "message": "인증되지 않은 사용자입니다.",
     *   "errorCode": "AUTH01",
     *   "data": null
     * }
     * }</pre>
     *
     * @param errorCode 인증/인가 영역의 오류 코드를 나타내는 열거형
     * @return HTTP 상태는 {@code errorCode.getHttpStatus()}이며, 바디는 메시지·내부 오류코드를 포함한 {@link BaseResponse}
     */
    public static ResponseEntity<BaseResponse<?>> failure(final ErrorAuthCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(BaseResponse.of(errorCode));
    }
}