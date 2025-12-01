package dev.syntax.global.auth.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CurrentUser
 *
 * <p>컨트롤러 메서드 파라미터에 현재 인증된 사용자 정보를 주입하기 위한
 * 커스텀 애노테이션입니다.</p>
 *
 * <p>{@code @AuthenticationPrincipal}을 직접 사용하는 대신,
 * 도메인 레벨에서 의도를 명확히 표현하기 위해 사용됩니다.
 * 이 애노테이션이 붙은 파라미터는
 * {@link dev.syntax.global.auth.resolver.CurrentUserArgumentResolver}
 * 를 통해 JWT 기반 사용자 정보(UserContext)로 변환됩니다.</p>
 *
 * <p>예시:</p>
 * <pre>{@code
 * @GetMapping("/profile")
 * public ResponseEntity<BaseResponse<?>> getProfile(@CurrentUser UserContext user) {
 *     return userService.getProfile(user.getId());
 * }
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
