package dev.syntax.domain.account.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.syntax.domain.account.dto.AccountHistoryReq;
import dev.syntax.domain.account.dto.AccountSummaryRes;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.service.AccountHistoryDetailService;
import dev.syntax.domain.account.service.AccountHistoryService;
import dev.syntax.domain.card.dto.CardInfoRes;
import dev.syntax.domain.card.service.CardInquiryService;
import dev.syntax.domain.transfer.dto.AutoTransferReq;
import dev.syntax.domain.transfer.dto.AutoTransferRes;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.transfer.service.AutoTransferCreateService;
import dev.syntax.domain.transfer.service.AutoTransferInquiryService;
import dev.syntax.domain.account.service.AccountBalanceService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;

/**
 * <h2>계좌 거래내역 조회 컨트롤러</h2>
 *
 * 계좌의 월별 거래내역 및 단일 거래 상세 조회 API를 제공하는 컨트롤러입니다.
 * <p>
 * - 자녀(본인)의 계좌 거래내역 조회<br>
 * - 부모가 자녀 계좌 거래내역 조회<br>
 * - 단일 거래 상세 조회<br>
 * </p>
 *
 * 모든 요청은 로그인한 사용자 정보(UserContext)를 기반으로 권한을 검증합니다.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

        private final AccountHistoryService accountHistoryService;
        private final AccountHistoryDetailService accountHistoryDetailService;
        private final AccountBalanceService accountSummaryService;
        private final CardInquiryService cardInquiryService;
        private final AutoTransferInquiryService autoTransferInquiryService;
        private final AutoTransferCreateService autoTransferCreateService;

        /**
         * 📌 [본인] 특정 계좌 잔액 조회
         * 예: GET /account/balance?type=ALLOWANCE
         */
        @GetMapping("/balance")
        public ResponseEntity<BaseResponse<?>> getMyBalance(
                        @CurrentUser UserContext user,
                        @RequestParam AccountType type) {

                return ApiResponseUtil.success(SuccessCode.OK,
                                accountSummaryService.getBalance(user, user.getId(), type));
        }

        /**
         * 📌 [부모 -> 자녀] 특정 계좌 잔액 조회
         * 예: GET /account/{childId}/balance?type=ALLOWANCE
         */
        @GetMapping("/{childId}/balance")
        public ResponseEntity<BaseResponse<?>> getChildBalance(
                        @CurrentUser UserContext user,
                        @PathVariable Long childId,
                        @RequestParam AccountType type) {

                return ApiResponseUtil.success(SuccessCode.OK,
                                accountSummaryService.getBalance(user, childId, type));
        }

        /**
         * 📌 본인 계좌 요약 조회 (자녀/부모 공통)
         *
         * GET /account/summary
         */
        @GetMapping("/summary")
        public ResponseEntity<BaseResponse<?>> getMySummary(
                        @CurrentUser UserContext user) {

                return ApiResponseUtil.success(SuccessCode.OK, accountSummaryService.getSummary(user, user.getId()));
        }

        /**
         * 📌 부모 → 자녀 계좌 요약 조회
         *
         * GET /account/{childId}/summary
         */
        @GetMapping("/{childId}/summary")
        public ResponseEntity<BaseResponse<?>> getChildSummary(
                        @CurrentUser UserContext user,
                        @PathVariable Long childId) {

                return ApiResponseUtil.success(SuccessCode.OK, accountSummaryService.getSummary(user, childId));
        }

        /**
         * <h3>📌 본인 계좌 거래내역 조회</h3>
         *
         * 로그인한 사용자의 특정 계좌(용돈·투자·저축)의 월별 거래내역을 조회합니다.
         * <p>
         * 자녀는 자신의 계좌 거래내역만 조회할 수 있으며, 부모는 이 API를 호출할 수 없습니다.
         * 부모는 {@link #getChildHistory(UserContext, Long, AccountHistoryReq)} API를 사용해야
         * 합니다.
         * </p>
         *
         * @param user 로그인한 사용자 컨텍스트 (JWT 기반)
         * @param req  거래내역 조회 조건 (계좌유형, 연도, 월)
         * @return 거래내역 리스트가 담긴 성공 응답
         *
         * @see AccountHistoryReq
         * @see AccountHistoryService#getHistory(Long, AccountHistoryReq, UserContext)
         */
        @GetMapping("/history")
        public ResponseEntity<BaseResponse<?>> getMyHistory(
                        @CurrentUser UserContext user,
                        @ModelAttribute AccountHistoryReq req) {
                return ApiResponseUtil.success(SuccessCode.OK,
                                accountHistoryService.getHistory(user.getId(), req, user));
        }

        /**
         * <h3>📌 부모가 자녀 계좌 거래내역 조회</h3>
         *
         * 부모가 연결된 자녀의 특정 계좌(용돈·저축·투자)의 월별 거래내역을 조회합니다.
         * <p>
         * 부모는 오직 연결 관계가 있는 자녀의 계좌만 조회할 수 있으며,
         * 인증 로직은 UserContext.children 목록 기준으로 판별합니다.
         * </p>
         *
         * @param user    로그인한 사용자 컨텍스트 (PARENT 권한)
         * @param childId 거래내역을 조회할 자녀 ID
         * @param req     거래내역 조회 조건 (계좌유형, 연도, 월)
         * @return 자녀 계좌의 거래내역 리스트가 담긴 성공 응답
         *
         * @throws dev.syntax.global.exception.BusinessException 권한 없을 때
         *                                                       (TX_NO_PERMISSION)
         * @see AccountHistoryService#getHistory(Long, AccountHistoryReq, UserContext)
         */
        @GetMapping("/{childId}/history")
        public ResponseEntity<BaseResponse<?>> getChildHistory(
                        @CurrentUser UserContext user,
                        @PathVariable Long childId,
                        @ModelAttribute AccountHistoryReq req) {
                return ApiResponseUtil.success(SuccessCode.OK,
                                accountHistoryService.getHistory(childId, req, user));
        }

        /**
         * <h3>📌 단일 거래 상세 조회</h3>
         *
         * 거래 ID를 통해 단일 거래 상세 정보를 조회합니다.
         * <p>
         * - 거래 금액<br>
         * - 거래처명(merchant)<br>
         * - 거래 타입(입금/출금)<br>
         * - 거래 카테고리<br>
         * - 승인 금액<br>
         * - 거래 일시<br>
         * - 거래 후 잔액(balanceAfter)<br>
         * </p>
         *
         * 조회 권한은 아래 규칙에 따라 검증됩니다:
         * <ul>
         * <li>자녀 → 본인 계좌의 거래만 조회 가능</li>
         * <li>부모 → 연결된 자녀의 계좌 거래만 조회 가능</li>
         * </ul>
         *
         * @param user          로그인한 사용자 컨텍스트
         * @param transactionId 조회할 거래 ID
         * @return 해당 거래의 상세 정보가 담긴 성공 응답
         *
         * @throws dev.syntax.global.exception.BusinessException
         * <ul>
         * <li>TX_INVALID_TRANSACTION_ID
         * – 잘못된 거래 ID</li>
         * <li>TX_NOT_FOUND – 거래가
         * 존재하지 않을 경우</li>
         * <li>TX_ACCOUNT_NOT_FOUND
         * – 거래가 속한 계좌 없음</li>
         * <li>TX_NO_PERMISSION –
         * 접근 권한 없음</li>
         * </ul>
         *
         * @see AccountHistoryDetailService#getDetail(Long, UserContext)
         */
        @GetMapping("/history/{transactionId}")
        public ResponseEntity<BaseResponse<?>> getDetail(
                        @CurrentUser UserContext user,
                        @PathVariable Long transactionId) {

                return ApiResponseUtil.success(SuccessCode.OK,
                                accountHistoryDetailService.getDetail(transactionId, user));
        }

            /**
         * 자녀 본인 카드 조회
         * GET /account/card
         */
        @GetMapping("/card")
        public ResponseEntity<BaseResponse<?>> getMyCard(@CurrentUser UserContext ctx) {
                CardInfoRes res = cardInquiryService.getCardInfo(ctx.getId(), ctx);
                return ApiResponseUtil.success(SuccessCode.OK, res);
        }

        /**
         * 부모가 자녀 카드 조회
         * GET /account/{childId}/card
         */
        @GetMapping("/{childId}/card")
        public ResponseEntity<BaseResponse<?>> getChildCard(
                @PathVariable Long childId,
                @CurrentUser UserContext ctx
        ) {
                CardInfoRes res = cardInquiryService.getCardInfo(childId, ctx);
                return ApiResponseUtil.success(SuccessCode.OK, res);
        }

            /**
         * 자동이체 설정 조회 API.
         * <p>
         * 이 경로는 용돈 자동이체만 접근가능하다는 전제 하에 구현하였습니다.
         * 자녀의 자동이체 설정 정보를 조회합니다.
         * </p>
         *
         * @param id 자녀 ID (URL 경로 변수)
         * @param ctx 인증된 사용자 컨텍스트
         * @return 자동이체 설정 정보 (200 OK)
         */
        @GetMapping("/{id}/auto-transfer")
        public ResponseEntity<BaseResponse<?>> getAutoTransfer(
                @PathVariable("id") Long id,
                @CurrentUser UserContext ctx) {

                AutoTransferRes res = autoTransferInquiryService.getAutoTransfer(id, AutoTransferType.ALLOWANCE, ctx);
                return ApiResponseUtil.success(SuccessCode.OK, res);
        }

        /**
         * 자동이체 설정 생성 API.
         * <p>
         * 부모가 자녀의 계좌로 자동이체를 설정합니다.
         * </p>
         *
         * @param id 자녀 ID (URL 경로 변수)
         * @param req 자동이체 설정 요청 정보 (총 금액, 이체일, 투자 비율)
         * @param ctx 인증된 사용자 컨텍스트 (부모 권한 확인용)
         * @return 생성 성공 응답 (201 Created)
         */
        @PostMapping("/{id}/auto-transfer")
        public ResponseEntity<BaseResponse<?>> createAutoTransfer(
                @PathVariable("id") Long id,
                @RequestBody AutoTransferReq req,
                @CurrentUser UserContext ctx) {
                autoTransferCreateService.createAutoTransfer(id, req, ctx);
                return ApiResponseUtil.success(SuccessCode.CREATED);
        }

}
