package dev.syntax.domain.report.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

import dev.syntax.domain.report.enums.Category;

@Getter
@AllArgsConstructor
public class CoreTransactionRes {
    private Category category;
    private BigDecimal amount;
}