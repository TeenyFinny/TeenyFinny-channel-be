package dev.syntax.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 필요한 권한이 존재하지 않는 경우 403 Forbidden 에러를 리턴하기 위한 클래스입니다.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 권한이 없는 리소스에 접근할 때 호출됩니다.
     *
     * @param request               HttpServletRequest
     * @param response              HttpServletResponse
     * @param accessDeniedException AccessDeniedException
     * @throws IOException      입출력 예외
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorAuthCode errorCode = ErrorAuthCode.ACCESS_DENIED;

        BaseResponse<?> body = BaseResponse.of(errorCode);

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}
