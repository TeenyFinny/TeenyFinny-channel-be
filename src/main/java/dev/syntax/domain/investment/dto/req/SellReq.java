package dev.syntax.domain.investment.dto.req;

import lombok.Data;

@Data
public class SellReq {
    private String cano;
    private String productCode;
    private String productName;
    private int quantity;
    private long price;
}