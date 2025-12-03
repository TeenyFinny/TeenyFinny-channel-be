package dev.syntax.domain.admin.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import dev.syntax.domain.admin.dto.CoreFailedTransactionListRes;
import dev.syntax.global.core.CoreApiProperties;
import dev.syntax.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreTransactionAdminClient {

	private final RestTemplate coreRestTemplate;
	private final CoreApiProperties properties;

	private static final String ADMIN_FAILED_TRANSACTION_URL =
		"/core/banking/admin/transaction/failed";

	public PageResponse<CoreFailedTransactionListRes> getFailedTransactions(
		boolean autoTransferOnly,
		int page,
		int size
	) {
		String url = properties.getBaseUrl() + ADMIN_FAILED_TRANSACTION_URL;

		String finalUrl = UriComponentsBuilder.fromHttpUrl(url)
			.queryParam("autoTransferOnly", autoTransferOnly)
			.queryParam("page", page)
			.queryParam("size", size)
			.queryParam("sort", "transactionDate,desc")
			.toUriString();

		log.info("[CORE ADMIN API] 실패 거래 조회: {}", finalUrl);

		ResponseEntity<PageResponse<CoreFailedTransactionListRes>> response =
			coreRestTemplate.exchange(
				finalUrl,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<PageResponse<CoreFailedTransactionListRes>>() {}
			);

		return response.getBody();
	}
}
