package dev.syntax.global.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * API 에러 응답의 기본 클래스입니다.
 * 에러 메시지를 포함하여 클라이언트에게 에러 상황을 전달합니다.
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
     * ApiCode 인터페이스 구현체로부터 에러 응답 객체를 생성합니다.
     *
     * @param apiMessage 에러 메시지를 제공하는 ApiCode 구현체
     * @return 생성된 BaseErrorResponse 객체
     */
    public static BaseErrorResponse<?> of(final ApiCode apiMessage) {
        return BaseErrorResponse.builder()
                .message(apiMessage.getMessage())
                .build();
    }

}
