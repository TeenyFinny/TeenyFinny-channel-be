package dev.syntax.domain.goal.controller;

import dev.syntax.domain.goal.dto.GoalDepositEventReq;
import dev.syntax.domain.goal.service.GoalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/internal/goal")
@RequiredArgsConstructor
public class GoalInternalController {

    private final GoalService goalService;

    /**
     * Core 서버에서 목표 계좌 입금 발생 시 호출하는 내부 전용 엔드포인트
     */
    @PostMapping("/deposit")
    public void onGoalDeposit(@RequestBody GoalDepositEventReq req) {
        log.info("[Core→Channel] 목표 계좌 입금 이벤트 수신: accountNo={}, balanceAfter={}",
                req.getAccountNo(), req.getBalanceAfter());

        goalService.handleGoalDeposit(req);
    }
}
