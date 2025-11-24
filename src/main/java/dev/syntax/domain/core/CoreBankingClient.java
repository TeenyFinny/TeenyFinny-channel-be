package dev.syntax.domain.core;

import dev.syntax.domain.core.dto.GoalAccountInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CoreBankingClient {

    public GoalAccountInfoDto getGoalTransactionInfo(Long goalId) {

        // ============================
        // TODO: core-banking 연동되면 WebClient 코드로 교체
        // ============================

        // ⭐ Mock 데이터로 임시 반환
        return new GoalAccountInfoDto(
                new BigDecimal("18000"),       // currentAmount
                List.of(
                        new BigDecimal("3000"),
                        new BigDecimal("3000"),
                        new BigDecimal("3000"),
                        new BigDecimal("3000"),
                        new BigDecimal("3000"),
                        new BigDecimal("3000")
                ),                            // depositAmounts
                List.of(
                        "2025.06.15 10:00:00",
                        "2025.07.15 10:00:00",
                        "2025.08.15 10:00:00",
                        "2025.09.15 10:00:00",
                        "2025.10.15 10:00:00",
                        "2025.11.15 10:00:00"
                )                             // depositTimes
        );
    }
}
