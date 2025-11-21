package dev.syntax.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 성공 응답을 나타내는 클래스입니다.
 * 성공 상태 코드와 함께 데이터 페이로드를 전달합니다.
 *
 * @param <T> 응답 데이터의 제네릭 타입
 */
@Getter
@Builder(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class SuccessResponse<T> implements BaseResponse<T> {

    @JsonProperty("data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * 성공 데이터로 응답 객체를 생성합니다.
     *
     * @param data 응답 본문에 포함할 데이터
     * @param <T>  데이터의 타입
     * @return 생성된 SuccessResponse 객체
     */
    public static <T> SuccessResponse<?> of(T data) {
        return SuccessResponse.builder()
                .data(data)
                .build();
    }

}
