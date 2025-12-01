package dev.syntax.global.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Core 뱅킹 서버 API 통신을 위한 설정 프로퍼티를 관리하는 클래스입니다.
 * <p>
 * application.yml 또는 application.properties 파일의 {@code core.api} 프리픽스로
 * 시작하는 프로퍼티들을 자동으로 바인딩합니다.
 * </p>
 * <p>
 * 설정 예시 (application.yml):
 * <pre>{@code
 * core:
 *   api:
 *     base-url: https://core-banking-api.example.com
 *     api-key: your-secret-api-key
 * }</pre>
 * </p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "core.api")
public class CoreApiProperties {

    /**
     * Core 서버의 기본 URL입니다.
     * <p>
     * 모든 Core API 호출 시 이 URL을 기준으로 요청이 전송됩니다.
     * 예: {@code https://core-banking-api.example.com}
     * </p>
     */
    private String baseUrl;

    /**
     * Core 서버 인증을 위한 API 키입니다.
     * <p>
     * 모든 Core API 요청의 {@code X-API-KEY} 헤더에 자동으로 포함되어
     * Core 서버 측에서 채널 서버를 인증하는 데 사용됩니다.
     * </p>
     */
    private String apiKey;
}
