package dev.syntax.domain.card.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.card.dto.CardCreateReq;
import dev.syntax.domain.card.dto.CardInfoRes;
import dev.syntax.domain.card.service.CardCreateService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Validated
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CardController {
    private final CardCreateService cardCreateService;
    
    /**
     * 📌 카드 발급 API
     * POST /allowance/cards
     */
    @PostMapping("/allowance/cards")
    public ResponseEntity<BaseResponse<?>> createCard(
            @CurrentUser UserContext ctx,
            @RequestBody CardCreateReq req) {

        CardInfoRes res = cardCreateService.createCard(req, ctx);

        return ApiResponseUtil.success(SuccessCode.CREATED, res);
    }
}
