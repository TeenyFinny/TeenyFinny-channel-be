package dev.syntax.domain.investment.dto.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CoreInvestTradeOrderReq {
	private String cano;
	private String productCode;
	private String productName;
	private int quantity;
	private String price;
}