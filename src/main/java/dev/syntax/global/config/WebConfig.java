package dev.syntax.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.syntax.global.auth.resolver.CurrentUserArgumentResolver;

/**
 * WebConfig
 *
 * <p>Spring MVC 전역 설정을 담당하는 구성 클래스입니다.
 * CORS 정책 설정과 커스텀 ArgumentResolver 등록을 수행하여
 * 컨트롤러 단의 사용성을 높입니다.</p>
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * CORS(Cross-Origin Resource Sharing) 정책을 설정합니다.
	 *
	 * <p>FE(Next.js / http://localhost:3000)와의 원활한 통신을 위해
	 * 모든 경로("/**")에 대해 GET, POST, PUT, PATCH, DELETE 요청을 허용합니다.</p>
	 *
	 * <p><b>OPTIONS 메소드는 브라우저가 실제 요청(PUT, DELETE 등)을 보내기 전에
	 * 서버에게 보안 정책을 확인하는 '프리플라이트(Preflight)' 요청에 사용됩니다.
	 * 따라서 안전하지 않은 메소드를 허용할 경우, OPTIONS 메소드도 반드시 허용해야 합니다.</b></p>
	 *
	 * @param registry CORS 설정을 위한 {@link CorsRegistry}
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
			.allowedOrigins("http://localhost:3000");
	}

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
