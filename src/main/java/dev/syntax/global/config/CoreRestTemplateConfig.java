package dev.syntax.global.config;

import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.core.CoreApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

/**
 * Core 서버 API 호출을 위한 RestTemplate 설정 클래스입니다.
 * <p>
 * 이 설정 클래스는 Core 서버와의 통신에 필요한 RestTemplate Bean을 생성하며,
 * 모든 요청에 자동으로 다음 헤더들을 추가합니다:
 * <ul>
 *     <li>X-API-KEY: Core 서버 인증용 API 키</li>
 *     <li>X-Core-User-Id: 현재 인증된 사용자의 Core User ID (인증된 경우)</li>
 * </ul>
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class CoreRestTemplateConfig {

    private final CoreApiProperties properties;

    /**
     * Core 서버 API 호출용 RestTemplate Bean을 생성합니다.
     * <p>
     * 생성된 RestTemplate은 요청 인터셉터를 통해 자동으로 다음 작업을 수행합니다:
     * <ol>
     *     <li>모든 요청에 X-API-KEY 헤더를 추가하여 Core 서버 인증을 처리합니다.</li>
     *     <li>SecurityContext에서 인증된 사용자 정보를 조회하여
     *         UserContext의 coreUserId가 존재하는 경우 X-Core-User-Id 헤더를 추가합니다.</li>
     * </ol>
     * </p>
     * <p>
     * 사용 예시:
     * <pre>{@code
     * @Autowired
     * private RestTemplate coreRestTemplate;
     * 
     * public void callCoreApi() {
     *     String response = coreRestTemplate.getForObject(
     *         properties.getBaseUrl() + "/api/endpoint",
     *         String.class
     *     );
     * }
     * }</pre>
     * </p>
     *
     * @return Core 서버 API 호출용으로 설정된 RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate coreRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

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
