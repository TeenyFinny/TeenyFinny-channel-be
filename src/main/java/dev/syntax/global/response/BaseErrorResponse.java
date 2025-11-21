package dev.syntax.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.syntax.global.response.error.ErrorAuthCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * ErrorBaseCode, SuccessCode, ErrorAuthCode 등의 열거형을 기반으로
 * API 응답 바디를 표준화하여 전달합니다.
 *
 * @param <T> 응답 데이터의 제네릭 타입
 */
@Getter
@SuperBuilder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class BaseErrorResponse<T> implements BaseResponse<T> {

    @JsonProperty("message")
    private String message;

    /**
     * 성공/실패 공통 인터페이스인 {@link ApiCode}로부터 메시지를 추출해 응답을 생성합니다.
     *
     * @param apiMessage 응답 메시지를 제공하는 ApiCode 구현체
     */
    public static BaseErrorResponse<?> of(final ApiCode apiMessage) {
        return BaseErrorResponse.builder()
                .message(apiMessage.getMessage())
                .build();
    }

}
