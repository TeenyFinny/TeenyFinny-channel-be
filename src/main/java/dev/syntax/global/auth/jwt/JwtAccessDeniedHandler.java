package dev.syntax.global.auth.jwt;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.syntax.global.response.AuthErrorResponse;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.error.ErrorAuthCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

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

		BaseResponse<?> body = AuthErrorResponse.of(errorCode);

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}