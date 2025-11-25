package dev.syntax.domain.account.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.syntax.domain.account.dto.core.CoreUserAccountListRes;
import dev.syntax.global.core.CoreApiProperties;
import lombok.RequiredArgsConstructor;

/**
 * Core 뱅킹 서버의 계좌 조회 API를 호출하는 클라이언트입니다.
 * <p>
 * 사용자의 전체 계좌 정보를 조회하며, 부모일 경우 자녀의 계좌까지 포함하여 반환합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CoreAccountClient {
	private final RestTemplate coreRestTemplate;
	private final CoreApiProperties properties;

	/** Core 서버의 계좌 조회 API 엔드포인트 */
	private static final String ACCOUNT_URL = "/core/banking/account";

	/**
	 * Core 서버에서 사용자의 전체 계좌 정보를 조회합니다.
	 * <p>
	 * 부모 사용자일 경우 자녀의 계좌 정보도 함께 반환됩니다.
	 * </p>
	 *
	 * @return 사용자 계좌 목록 (부모일 경우 자녀 계좌 포함)
	 * @throws dev.syntax.global.exception.CoreApiException Core 서버 API 호출 중 에러 발생 시
	 */
	public CoreUserAccountListRes getUserAccounts() {
		return coreRestTemplate.getForObject(
			properties.getBaseUrl() + ACCOUNT_URL,
			CoreUserAccountListRes.class
		);
	}
}
