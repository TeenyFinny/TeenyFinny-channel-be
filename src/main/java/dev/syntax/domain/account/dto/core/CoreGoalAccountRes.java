package dev.syntax.domain.account.dto.core;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoreGoalAccountRes {
    private String accountNumber;  // 코어에서 보내주는 계좌 번호
    private Long userId;            // 코어에서 보내주는 사용자 ID
    private BigDecimal balance;     // 초기 예수금
}
