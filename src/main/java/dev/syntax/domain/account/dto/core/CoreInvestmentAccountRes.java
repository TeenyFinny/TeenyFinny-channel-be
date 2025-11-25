package dev.syntax.domain.account.dto.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoreInvestmentAccountRes {
    private String cano;  // 코어 서버에서 보내주는 계좌 번호
}
