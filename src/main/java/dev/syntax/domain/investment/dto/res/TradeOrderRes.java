package dev.syntax.domain.investment.dto.res;

import dev.syntax.domain.investment.enums.OrderStatus;
import dev.syntax.domain.investment.enums.TradeType;
import lombok.Data;

@Data
public class TradeOrderRes {
    private TradeType tradeType;

    private String productCode;

    private String productName;

    private long quantity;

    private String price;

    private OrderStatus status;
}
