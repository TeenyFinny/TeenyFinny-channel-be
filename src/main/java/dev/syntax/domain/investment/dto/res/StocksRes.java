package dev.syntax.domain.investment.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.syntax.domain.investment.dto.PriceItem;
import lombok.Data;

import java.util.List;

@Data
public class StocksRes {
    @JsonProperty("output")
    private List<PriceItem> output;
}
