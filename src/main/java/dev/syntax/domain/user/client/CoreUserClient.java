package dev.syntax.domain.user.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.syntax.domain.user.dto.CoreChildInitRes;
import dev.syntax.domain.user.dto.CoreParentInitRes;
import dev.syntax.domain.user.dto.CoreUserInitReq;
import dev.syntax.global.core.CoreApiProperties;
import lombok.RequiredArgsConstructor;

/**
 * Core 뱅킹 서버의 사용자 계정 관리 API를 호출하는 클라이언트입니다.
 * <p>
 * 부모 및 자녀 사용자의 뱅킹 계정 생성을 담당하며,
 * Core 서버로부터 Core User ID를 받아 반환합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CoreUserClient {
	private final RestTemplate coreRestTemplate;
	private final CoreApiProperties properties;

	/** Core 서버의 사용자 초기화 API 엔드포인트 */
	private static final String SIGNUP_URL = "/core/banking/init";

	/**
	 * Core 서버에 부모 사용자의 뱅킹 계정을 생성합니다.
	 *
	 * @param req 사용자 초기화 요청 DTO
	 * @return 부모 계정 생성 응답 (Core User ID, 계좌 정보 포함)
	 * @throws dev.syntax.global.exception.CoreApiException Core 서버 API 호출 중 에러 발생 시
	 */
	public CoreParentInitRes createParentAccount(CoreUserInitReq req) {
		return postToCore(req, CoreParentInitRes.class);
	}

	/**
	 * Core 서버에 자녀 사용자의 뱅킹 계정을 생성합니다.
	 *
	 * @param req 사용자 초기화 요청 DTO
	 * @return 자녀 계정 생성 응답 (Core User ID 포함)
	 * @throws dev.syntax.global.exception.CoreApiException Core 서버 API 호출 중 에러 발생 시
	 */
	public CoreChildInitRes createChildUser(CoreUserInitReq req) {
		return postToCore(req, CoreChildInitRes.class);
	}

	private <T> T postToCore(CoreUserInitReq req, Class<T> responseType) {
		return coreRestTemplate.postForObject(
			properties.getBaseUrl() + SIGNUP_URL,
			req,
			responseType
		);
	}
}
