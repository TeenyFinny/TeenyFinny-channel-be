package dev.syntax.domain.investment.dto.res;

import dev.syntax.domain.investment.enums.OrderStatus;
import dev.syntax.domain.investment.enums.TradeType;

public class TradeOrder {
    private TradeType tradeType;

    private String productCode;

    private String productName;

    private Integer quantity;

    private Long price;

    private OrderStatus status = OrderStatus.EXECUTED;
}
