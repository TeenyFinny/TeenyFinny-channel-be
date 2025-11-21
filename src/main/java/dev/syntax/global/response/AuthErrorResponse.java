package dev.syntax.global.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.syntax.global.response.error.ErrorAuthCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 인증/인가 관련 에러 응답을 나타내는 클래스입니다.
 * 기본 에러 응답에 더해 상세 에러 코드를 포함합니다.
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
     * ErrorAuthCode로부터 인증/인가 에러 응답 객체를 생성합니다.
     *
     * @param errorCode 인증/인가 에러 코드
     * @return 생성된 AuthErrorResponse 객체
     */
    public static AuthErrorResponse<?> of(ErrorAuthCode errorCode) {
        return AuthErrorResponse.builder()
                .message(errorCode.getMessage())
                .errorCode(errorCode.getErrorCode())
                .build();
    }
}
