package dev.syntax.domain.investment.dto.res;

import dev.syntax.domain.investment.dto.PriceItem;
import java.util.List;
import lombok.Data;

@Data
public class StocksRes {
    private List<PriceItem> output;
}
