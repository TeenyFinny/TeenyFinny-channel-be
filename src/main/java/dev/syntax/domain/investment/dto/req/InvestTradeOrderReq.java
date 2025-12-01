package dev.syntax.domain.investment.dto.req;

import lombok.Data;

@Data
public class InvestTradeOrderReq {
	private String productCode;
	private String productName;
	private int quantity;
	private String price;
}