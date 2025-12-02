package dev.syntax.domain.investment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PriceItem {
    @JsonProperty("inter_shrn_iscd")
    private String productCode;   // 종목코드

    @JsonProperty("inter_kor_isnm")
    private String productName;   // 종목명

    @JsonProperty("inter2_prpr")
    private String currentPrice; // 현재가

    @JsonProperty("inter2_prdy_vrss")
    private String prevPriceChange; // 전일 대비 금액

    @JsonProperty("prdy_ctrt")
    private String prevRate; // 전일 대비율

    @JsonProperty("acml_vol")
    private String accumulatedVolume; // 누적 거래량

}