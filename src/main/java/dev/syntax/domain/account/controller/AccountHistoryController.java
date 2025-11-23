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

@Slf4j
@Validated
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountHistoryController { // 클래스명 오타(Accout -> Account)도 확인해보세요!

    private final AccountHistoryService accountHistoryService;

    @GetMapping("/history")
    public ResponseEntity<BaseResponse<?>> getMyHistory(
            @CurrentUser UserContext user,
            @ModelAttribute AccountHistoryReq req) {
        return ApiResponseUtil.success(SuccessCode.OK,
                accountHistoryService.getHistory(user.getId(), req, user));
    }

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