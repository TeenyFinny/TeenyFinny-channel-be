package dev.syntax.domain.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminAutoTransferRes {

	private Long id;
	private Long userId;
	private Long fromAccountId;
	private Long toAccountId;
	private BigDecimal amount;
	private String memo;
	private Integer transferDay;
	private LocalDate nextTransferDay;
	private String status;
	private LocalDateTime createdAt;

	public static AdminAutoTransferRes from(CoreAutoTransferListRes core) {
		return AdminAutoTransferRes.builder()
			.id(core.getId())
			.userId(core.getUserId())
			.fromAccountId(core.getFromAccountId())
			.toAccountId(core.getToAccountId())
			.amount(core.getAmount())
			.memo(core.getMemo())
			.transferDay(core.getTransferDay())
			.nextTransferDay(core.getNextTransferDay())
			.status(core.getStatus())
			.createdAt(core.getCreatedAt())
			.build();
	}
}
