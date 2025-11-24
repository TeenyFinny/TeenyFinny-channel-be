package dev.syntax.domain.account.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.account.dto.AccountHistoryReq;
import dev.syntax.domain.account.service.AccountHistoryService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;

/**
 * 계좌 거래내역 조회 컨트롤러.
 *
 * <p>
 * 사용자의 계좌 거래내역을 월별로 조회하는 API를 제공합니다.
 * 본인 계좌 조회 및 부모가 자녀의 계좌를 조회하는 기능을 포함합니다.
 * </p>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountHistoryController {

    private final AccountHistoryService accountHistoryService;

    /**
     * 본인 계좌 거래내역 조회.
     *
     * <p>
     * 로그인한 사용자의 특정 계좌에 대한 월별 거래내역을 조회합니다.
     * </p>
     *
     * @param user 로그인한 사용자 컨텍스트 (UserContext)
     * @param req  거래내역 조회 조건 (계좌유형, 연도, 월)
     * @return 거래내역 리스트가 담긴 성공 응답
     */
    @GetMapping("/history")
    public ResponseEntity<BaseResponse<?>> getMyHistory(
            @CurrentUser UserContext user,
            @ModelAttribute AccountHistoryReq req) {
        return ApiResponseUtil.success(SuccessCode.OK,
                accountHistoryService.getHistory(user.getId(), req, user));
    }

    /**
     * 자녀 계좌 거래내역 조회 (부모용).
     *
     * <p>
     * 부모가 연결된 자녀의 특정 계좌에 대한 월별 거래내역을 조회합니다.
     * </p>
     *
     * @param user    로그인한 사용자 컨텍스트 (UserContext)
     * @param childId 조회할 자녀의 ID (PathVariable)
     * @param req     거래내역 조회 조건 (계좌유형, 연도, 월)
     * @return 거래내역 리스트가 담긴 성공 응답
     */
    @GetMapping("/{childId}/history")
    public ResponseEntity<BaseResponse<?>> getChildHistory(
            @CurrentUser UserContext user,
            @PathVariable Long childId,
            @ModelAttribute AccountHistoryReq req) {    
        return ApiResponseUtil.success(
                SuccessCode.OK,
                accountHistoryService.getHistory(childId, req, user));
    }
}