package dev.syntax.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.syntax.global.auth.resolver.CurrentUserArgumentResolver;

/**
 * WebConfig
 *
 * <p>Spring MVC 전역 설정을 담당하는 구성 클래스입니다.
 * 커스텀 ArgumentResolver 등록을 수행하여 컨트롤러 단의 사용성을 높입니다.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * 커스텀 ArgumentResolver를 등록합니다.
	 *
	 * <p>{@link CurrentUserArgumentResolver}는
	 * 컨트롤러 메서드에서 {@code @CurrentUser} 애노테이션을
	 * 통해 JWT 기반 인증된 사용자 정보를 자동으로 주입받을 수 있도록 합니다.</p>
	 *
	 * @param resolvers Spring MVC가 사용할 ArgumentResolver 목록
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new CurrentUserArgumentResolver());
	}
}
