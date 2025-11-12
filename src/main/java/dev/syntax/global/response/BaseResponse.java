package dev.syntax.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.syntax.global.response.error.ErrorAuthCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ErrorBaseCode, SuccessCode, ErrorAuthCode 등의 열거형을 기반으로
 * API 응답 바디를 표준화하여 전달합니다.
 *
 * @param <T> 응답 데이터의 제네릭 타입
 */
@Getter
@Builder(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class BaseResponse<T> {

    @JsonProperty("message")
    private String message;

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * 성공/실패 공통 인터페이스인 {@link ApiCode}로부터 메시지를 추출해 응답을 생성합니다.
     *
     * @param apiMessage 응답 메시지를 제공하는 ApiCode 구현체
     */
    public static BaseResponse<?> of(final ApiCode apiMessage) {
        return BaseResponse.builder()
                .message(apiMessage.getMessage())
                .build();
    }

    /**
     * 성공 코드와 함께 데이터 페이로드를 포함한 응답을 생성합니다.
     *
     * @param successCode 성공 코드를 나타내는 열거형
     * @param data        응답 본문에 포함할 데이터
     */
    public static <T> BaseResponse<?> of(SuccessCode successCode, T data) {
        return BaseResponse.builder()
                .message(successCode.getMessage())
                .data(data)
                .build();
    }

    /**
     * 인증/인가 실패 등 Auth 관련 오류 코드를 기반으로 오류 응답을 생성합니다.
     *
     * @param errorCode Auth 영역의 오류 코드를 나타내는 열거형
     */
    public static BaseResponse<?> of(ErrorAuthCode errorCode) {
        return BaseResponse.builder()
                .errorCode(errorCode.getErrorCode())
                .message(errorCode.getMessage())
                .build();
    }
}