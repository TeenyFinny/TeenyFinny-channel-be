package dev.syntax.global.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 성공 응답을 표현하는 열거형입니다.
 * <p>
 * {@link ApiCode}를 <b>구현</b>하여 각 항목별로 HTTP 상태 코드와 사용자 메시지를 제공합니다.
 * 컨트롤러/서비스 계층에서 공통 성공 응답을 일관되게 리턴할 때 사용합니다.
 * </p>
 *
 * <h3>사용 예</h3>
 * <pre>{@code
 * return ApiResponseUtil.success(SuccessCode.OK);              // 바디 없이 성공
 * return ApiResponseUtil.success(SuccessCode.CREATED, data);   // 생성 성공 + 데이터
 * }</pre>
 *
 * <p><b>확장 가이드:</b> 도메인별 성공 케이스가 필요하면 새 상수를 추가하고
 * 사용자 메시지를 서비스/제품 톤앤매너에 맞춰 관리하세요.
 * </p>
 *
 * @see ApiCode
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SuccessCode implements ApiCode {
    /**
     * 200 OK
     */
    OK(HttpStatus.OK, "요청이 성공했습니다."),

    /**
     * 201 CREATED
     */
    CREATED(HttpStatus.CREATED, "요청이 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}