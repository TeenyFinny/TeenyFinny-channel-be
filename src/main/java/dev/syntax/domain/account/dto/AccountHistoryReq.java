package dev.syntax.domain.account.dto;

import dev.syntax.domain.account.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 계좌 거래내역 조회 요청 DTO.
 *
 * <p>
 * 특정 사용자의 계좌에 대해 월별 거래내역을 조회할 때 사용하는 요청 객체이다.
 * 부모/자녀 여부에 따라 조회 대상 사용자는 Controller 또는 Service 단에서
 * UserContext를 기반으로 판별되며, 이 DTO는 순수하게 조회 조건만을 전달한다.
 * </p>
 *
 * <ul>
 * <li>{@code accountType} : 조회하고자 하는 계좌의 유형
 * (예: ALLOWANCE, GOAL, INVEST)</li>
 * <li>{@code year} : 조회 연도 (예: 2025)</li>
 * <li>{@code month} : 조회 월 (1~12)</li>
 * </ul>
 *
 * <p>
 * 예시 호출:
 * </p>
 * 
 * <pre>
 * GET /account/history?accountType=ALLOWANCE&year=2025&month=10
 * GET /account/{childId}/history?accountType=GOAL&year=2025&month=11
 * </pre>
 */
public record AccountHistoryReq(
        @NotNull(message = "시작일(startDate)은 필수입니다.")
        LocalDate startDate,

        @NotNull(message = "종료일(endDate)은 필수입니다.")
        LocalDate endDate) {
}
