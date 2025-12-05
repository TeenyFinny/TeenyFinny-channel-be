package dev.syntax.domain.goal.controller;

import dev.syntax.domain.goal.dto.GoalDepositEventReq;
import dev.syntax.domain.goal.service.GoalService;
import dev.syntax.global.core.CoreApiProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/internal/goal")
@RequiredArgsConstructor
public class GoalInternalController {

    private final GoalService goalService;
    private final CoreApiProperties coreApiProperties;

    /**
     * Core 서버에서 목표 계좌 입금 발생 시 호출하는 내부 전용 엔드포인트
     */
    @PostMapping("/deposit")
    public void onGoalDeposit(
            @RequestBody GoalDepositEventReq req,
            HttpServletRequest request) {
        
        // API-KEY 검증
        String apiKey = request.getHeader("X-API-KEY");
        String expectedApiKey = coreApiProperties.getApiKey();
        
        if (expectedApiKey == null || !expectedApiKey.equals(apiKey)) {
            log.warn("[Core→Channel] API-KEY 검증 실패. 요청 URI: {}", request.getRequestURI());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API Key");
        }
        
        log.info("[Core→Channel] 목표 계좌 입금 이벤트 수신: accountNo={}, balanceAfter={}",
                req.getAccountNo(), req.getBalanceAfter());

        goalService.handleGoalDeposit(req);
    }
}
