package dev.syntax.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.syntax.global.response.error.ErrorAuthCode;
import lombok.AllArgsConstructor;
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
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class AuthErrorResponse<T> extends BaseErrorResponse<T> {

    @JsonProperty("errorCode")
    private String errorCode;

    /**
     * 인증/인가 실패 등 Auth 관련 오류 코드를 기반으로 오류 응답을 생성합니다.
     *
     * @param errorCode Auth 영역의 오류 코드를 나타내는 열거형
     */
    public static AuthErrorResponse<?> of(ErrorAuthCode errorCode) {
        return AuthErrorResponse.builder()
                .message(errorCode.getMessage())
                .errorCode(errorCode.getErrorCode())
                .build();
    }
}
