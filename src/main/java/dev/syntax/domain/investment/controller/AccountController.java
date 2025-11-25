package dev.syntax.domain.investment.controller;

import dev.syntax.domain.investment.dto.res.AccountRes;
import dev.syntax.domain.investment.service.AccountService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments")
public class AccountController {
    private final AccountService accountService;


    @GetMapping("/account")
    public ResponseEntity<BaseResponse<?>> getCanoByUserId (
            @CurrentUser UserContext userContext
    ) {
        String res = accountService.getCanoByUserId(userContext.getId());
        return ApiResponseUtil.success(SuccessCode.OK, res);
    }

    @PostMapping("/account")
    public ResponseEntity<BaseResponse<?>> createInvestmentAccount(
            @CurrentUser UserContext userContext
    ) {
        AccountRes res = accountService.createInvestmentAccount(userContext.getId());
        return ApiResponseUtil.success(SuccessCode.CREATED, res);
    }
}
