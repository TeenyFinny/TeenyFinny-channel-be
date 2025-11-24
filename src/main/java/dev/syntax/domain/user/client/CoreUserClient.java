package dev.syntax.domain.user.client;

import dev.syntax.domain.user.dto.CoreChildInitRes;
import dev.syntax.domain.user.dto.CoreParentInitRes;
import dev.syntax.domain.user.dto.CoreUserInitReq;
import dev.syntax.global.config.CoreRestTemplateConfig;
import dev.syntax.global.core.CoreApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Core 뱅킹 서버의 사용자 계정 관리 API를 호출하는 클라이언트 클래스입니다.
 * <p>
 * 이 클라이언트는 Channel 서버에서 Core 서버로 사용자 계정 생성 요청을 전송합니다.
 * Core 서버는 사용자 정보를 받아 뱅킹 계정을 초기화하고, Core User ID를 반환합니다.
 * </p>
 * <p>
 * RestTemplate을 사용하여 HTTP 요청을 전송하며,
 * {@link CoreRestTemplateConfig}에서 설정된 인터셉터를 통해
 * 자동으로 X-API-KEY 헤더가 추가됩니다.
 * </p>
 *
 * @see CoreRestTemplateConfig
 * @see CoreApiProperties
 */
@Service
@RequiredArgsConstructor
public class CoreUserClient {
    private final RestTemplate coreRestTemplate;
    private final CoreApiProperties properties;

    /**
     * Core 서버의 사용자 초기화 API 엔드포인트 경로입니다.
     */
    private final String SIGNUP_URL = "/core/banking/init";

    /**
     * Core 서버에 부모 사용자의 뱅킹 계정을 생성합니다.
     * <p>
     * 부모 회원가입 시 호출되며, Core 서버는 부모 계정과 일반 입출금 계좌를 생성합니다.
     * 생성된 Core User ID와 계좌 번호는 응답으로 반환되어 Channel 서버의 User 엔티티에 저장됩니다.
     * </p>
     *
     * @param req Core 사용자 초기화 요청 DTO (사용자명, 생년월일 등 포함)
     * @return 부모 계정 생성 응답 (Core User ID, 일반 입출금 계좌 정보 포함)
     */
    public CoreParentInitRes createParentAccount(CoreUserInitReq req) {
        return coreRestTemplate.postForObject(
                properties.getBaseUrl() + SIGNUP_URL,
                req,
                CoreParentInitRes.class
        );
    }
    /**
     * Core 서버에 자녀 사용자의 뱅킹 계정을 생성합니다.
     * <p>
     * 자녀 회원가입 시 호출되며, Core 서버는 자녀 계정을 생성합니다.
     * 생성된 Core User ID는 응답으로 반환되어 Channel 서버의 User 엔티티에 저장됩니다.
     * </p>
     *
     * @param req Core 사용자 초기화 요청 DTO (사용자명, 생년월일 등 포함)
     * @return 자녀 계정 생성 응답 (Core User ID 포함)
     */
    public CoreChildInitRes createChildAccount(CoreUserInitReq req) {
        return coreRestTemplate.postForObject(
                properties.getBaseUrl() + SIGNUP_URL,
                req,
                CoreChildInitRes.class
        );
    }
}
