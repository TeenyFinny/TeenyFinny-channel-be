package dev.syntax.global.response;

import org.springframework.http.HttpStatus;

/**
 * 가장 기본적인 API 응답 인터페이스입니다. <br>
 * ErrorCode, SuccessCode에 상속됩니다.
 */
public interface ApiCode {
    HttpStatus getHttpStatus();
    String getMessage();
}