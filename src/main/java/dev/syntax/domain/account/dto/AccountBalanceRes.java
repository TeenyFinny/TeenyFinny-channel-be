package dev.syntax.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccountBalanceRes {
    private BigDecimal balance;
}
