package dev.syntax.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 유효한 자격증명을 제공하지 않고 접근하려 할 때 401 Unauthorized 에러를 리턴하기 위한 클래스입니다.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 호출됩니다.
     *
     * @param request       HttpServletRequest
     * @param response      HttpServletResponse
     * @param authException AuthenticationException
     * @throws IOException      입출력 예외
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        ErrorAuthCode errorCode = ErrorAuthCode.UNAUTHORIZED;

        BaseResponse<?> body = BaseResponse.of(errorCode);

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}