package dev.syntax.domain.admin.client;

import java.time.LocalDate;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import dev.syntax.domain.admin.dto.CoreAutoTransferListRes;
import dev.syntax.global.core.CoreApiProperties;
import dev.syntax.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoreAutoTransferAdminClient {

	private final RestTemplate coreRestTemplate;
	private final CoreApiProperties properties;

	private static final String ADMIN_AUTO_TRANSFER_URL = "/core/banking/admin/auto-transfer";

	public PageResponse<CoreAutoTransferListRes> getAutoTransferList(
		String status,
		LocalDate startDate,
		LocalDate endDate,
		int page,
		int size
	) {
		String url = properties.getBaseUrl() + ADMIN_AUTO_TRANSFER_URL;

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
			.queryParam("page", page)
			.queryParam("size", size)
			.queryParam("sort", "createdAt,desc");

		if (status != null && !status.isEmpty()) {
			builder.queryParam("status", status);
		}
		if (startDate != null) {
			builder.queryParam("startDate", startDate.toString());
		}
		if (endDate != null) {
			builder.queryParam("endDate", endDate.toString());
		}

		String finalUrl = builder.toUriString();
		log.info("[CORE ADMIN API] 자동이체 목록 조회: {}", finalUrl);

		ResponseEntity<PageResponse<CoreAutoTransferListRes>> response =
			coreRestTemplate.exchange(
				finalUrl,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<PageResponse<CoreAutoTransferListRes>>() {}
			);

		return response.getBody();
	}

	public void executeAutoTransfer(Long autoTransferId) {
		String url = properties.getBaseUrl()
			+ ADMIN_AUTO_TRANSFER_URL
			+ "/" + autoTransferId
			+ "/execute";

		log.info("[CORE ADMIN API] 자동이체 수동 실행: {}", autoTransferId);

		coreRestTemplate.postForEntity(url, null, Void.class);
	}
}
