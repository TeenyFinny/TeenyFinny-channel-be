package dev.syntax.domain.report.controller;


import dev.syntax.domain.report.dto.ReportRes;
import dev.syntax.domain.report.service.ReportService;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/allowance")
public class ReportController {

    private final ReportService reportService;

    /**
     * 자녀 본인 리포트 조회
     * GET /allowance/report?month=1
     */
    @GetMapping("/report")
    public ResponseEntity<BaseResponse<?>> getMyReport(
            @RequestParam int month,
            @AuthenticationPrincipal UserContext ctx
    ) {
        ReportRes result = reportService.getMonthlyReport(ctx.getId(), month, ctx);
        return ApiResponseUtil.success(SuccessCode.OK, result);
    }

    /**
     * 부모가 자녀 리포트 조회
     * GET /allowance/{childId}/report?month=1
     */
    @GetMapping("/{childId}/report")
    public ResponseEntity<BaseResponse<?>> getChildReport(
            @PathVariable Long childId,
            @RequestParam int month,
            @AuthenticationPrincipal UserContext ctx
    ) {
        ReportRes result = reportService.getMonthlyReport(childId, month, ctx);
        return ApiResponseUtil.success(SuccessCode.OK, result);
    }
}