package dev.syntax.domain.transfer.dto;

import java.math.BigDecimal;

public record CoreAllowanceUpdateAutoTransferReq (
    BigDecimal amount,
    Integer transferDay
){}