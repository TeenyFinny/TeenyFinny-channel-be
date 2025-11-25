package dev.syntax.domain.investment.dto.res;

import dev.syntax.domain.investment.dto.InvestmentAccount;
import dev.syntax.domain.investment.enums.OrderStatus;
import dev.syntax.domain.investment.enums.TradeType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Builder;

public class TradeOrderRes {
    private Long id;

    private InvestmentAccount cano;

    private Long userId;

    private String globalUid;

    private LocalDateTime orderTime;

    private TradeType tradeType;

    private String productCode;

    private String productName;

    private Integer quantity;

    private Long price;

    private String exchangeDivisionCode;

    private OrderStatus status = OrderStatus.REQUESTED;
}
