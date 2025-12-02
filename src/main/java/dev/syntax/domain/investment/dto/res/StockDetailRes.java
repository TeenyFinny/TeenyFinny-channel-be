package dev.syntax.domain.investment.dto.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockDetailRes {

    private String productCode;      // 종목코드
    private String productName;      // 종목명
    private long currentPrice;       // 현재가
    private String prevRate;         // 전일대비율
    private String accumulatedVolume;// 거래량

    private long depositAmount;      // 예수금
    private int maxBuyQuantity;      // 최대 매수 가능 수량
    private long holdingQuantity;    // 보유 수량
}
