package dev.syntax.global.config;

import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.core.CoreApiProperties;
import dev.syntax.global.core.CoreRestTemplateErrorHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

/**
 * Core 서버 API 호출을 위한 RestTemplate 설정 클래스입니다.
 * <p>
 * 모든 요청에 X-API-KEY와 X-Core-User-Id 헤더를 자동으로 추가하며,
 * 에러 발생 시 {@link dev.syntax.global.core.CoreRestTemplateErrorHandler}를 통해 처리합니다.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class CoreRestTemplateConfig {

    private final CoreApiProperties properties;
    private final CoreRestTemplateErrorHandler errorHandler;

    /**
     * Core 서버 API 호출용 RestTemplate Bean을 생성합니다.
     * <p>
     * 모든 요청에 X-API-KEY와 X-Core-User-Id 헤더를 자동으로 추가하며,
     * 에러 발생 시 CoreRestTemplateErrorHandler를 통해 처리합니다.
     * </p>
     *
     * @return Core 서버 API 호출용 RestTemplate
     */
    @Bean
    public RestTemplate coreRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 에러 핸들러 설정
        restTemplate.setErrorHandler(errorHandler);

        restTemplate.getInterceptors().add((request, body, execution) -> {

            // API-KEY 추가
            request.getHeaders().add("X-API-KEY", properties.getApiKey());

            // userContext 기반 X-Core-User-Id 추가
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserContext user) {
                if (user.getCoreUserId() != null) {
                    request.getHeaders().add("X-Core-User-Id", String.valueOf(user.getCoreUserId()));
                }
            }

            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
