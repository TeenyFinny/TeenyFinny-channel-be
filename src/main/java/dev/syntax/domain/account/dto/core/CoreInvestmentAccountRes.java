package dev.syntax.domain.account.dto.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoreInvestmentAccountRes {
    private String accountNumber;  // 코어에서 보내주는 계좌 번호
    private Long userId;            // 코어에서 보내주는 사용자 ID
    private Long balance;     // 초기 예수금
}
