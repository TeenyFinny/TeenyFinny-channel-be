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
public interface BaseResponse<T> {

}