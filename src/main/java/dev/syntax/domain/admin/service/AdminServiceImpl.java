package dev.syntax.domain.admin.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import dev.syntax.domain.admin.client.CoreAutoTransferAdminClient;
import dev.syntax.domain.admin.client.CoreTransactionAdminClient;
import dev.syntax.domain.admin.dto.AdminAutoTransferRes;
import dev.syntax.domain.admin.dto.AdminFailedTransactionRes;
import dev.syntax.domain.admin.dto.CoreAutoTransferListRes;
import dev.syntax.domain.admin.dto.CoreFailedTransactionListRes;
import dev.syntax.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

	private final CoreAutoTransferAdminClient coreAutoTransferAdminClient;
	private final CoreTransactionAdminClient coreTransactionAdminClient;

	@Override
	public Page<AdminAutoTransferRes> getAutoTransferList(
		String status,
		LocalDate startDate,
		LocalDate endDate,
		int page,
		int size
	) {
		log.info("[ADMIN] 자동이체 조회(page={}, size={})", page, size);

		PageResponse<CoreAutoTransferListRes> corePage =
			coreAutoTransferAdminClient.getAutoTransferList(status, startDate, endDate, page, size);

		log.info("[ADMIN] Core 자동이체 응답 수신(content={})", corePage.getContent().size());

		return new PageImpl<>(
			corePage.getContent().stream()
				.map(AdminAutoTransferRes::from)
				.toList(),
			PageRequest.of(corePage.getPage(), corePage.getSize()),
			corePage.getTotalElements()
		);
	}

	@Override
	public void executeAutoTransfer(Long autoTransferId) {
		log.info("[ADMIN] 자동이체 수동 실행 요청(id={})", autoTransferId);
		coreAutoTransferAdminClient.executeAutoTransfer(autoTransferId);
	}

	@Override
	public Page<AdminFailedTransactionRes> getFailedTransactions(
		boolean autoTransferOnly,
		int page,
		int size
	) {
		log.info("[ADMIN] 실패 거래 조회(page={}, size={}, autoOnly={})",
			page, size, autoTransferOnly);

		PageResponse<CoreFailedTransactionListRes> corePage =
			coreTransactionAdminClient.getFailedTransactions(autoTransferOnly, page, size);

		log.info("[ADMIN] Core 실패 거래 응답(content={})", corePage.getContent().size());

		return new PageImpl<>(
			corePage.getContent().stream()
				.map(AdminFailedTransactionRes::from)
				.toList(),
			PageRequest.of(corePage.getPage(), corePage.getSize()),
			corePage.getTotalElements()
		);
	}
}
