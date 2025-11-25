package dev.syntax.domain.investment.dto.req;

import lombok.Data;

@Data
public class BuyReq {
    private String cano;
    private Long userId;
    private String productCode;
    private String productName;
    private int quantity;
    private long price;
}