package dev.syntax.global.auth.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import dev.syntax.domain.auth.dto.UserContext;
import dev.syntax.global.auth.annotation.CurrentUser;

/**
 * CurrentUserArgumentResolver
 *
 * <p>{@link CurrentUser} 애노테이션이 붙은 컨트롤러 파라미터에
 * JWT 기반으로 인증된 사용자 정보를 자동 주입하는 Resolver입니다.</p>
 *
 * <p>Spring Security의 {@link SecurityContextHolder}에서 Authentication 객체를 가져와
 * 그 안에 저장된 Principal(UserContext)을 반환합니다.
 * 인증이 되어 있지 않거나 Principal이 비어있을 경우 {@code null}을 반환합니다.</p>
 *
 * <p>이 Resolver는 WebConfig의 {@code addArgumentResolvers}를 통해 등록되어 동작합니다.</p>
 *
 * <p><b>사용 예시</b>:</p>
 * <pre>{@code
 * @GetMapping("/me")
 * public ApiResponse<?> getMyInfo(@CurrentUser UserContext user) {
 *     return userService.getUserInfo(user.getId());
 * }
 * }</pre>
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

	/**
	 * @return 해당 파라미터가 {@link CurrentUser} 애노테이션을 가지고 있고,
	 *         타입이 UserContext일 때 true를 반환합니다.
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentUser.class)
			&& parameter.getParameterType().equals(UserContext.class);
	}

	/**
	 * SecurityContext에서 인증 객체를 추출하여 UserContext를 반환합니다.
	 *
	 * @return 인증된 사용자(UserContext) 혹은 인증되어 있지 않으면 null
	 */
	@Override
	public Object resolveArgument(
		MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory
	) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || authentication.getPrincipal() == null) {
			return null;
		}

		return authentication.getPrincipal();
	}
}
