package dev.syntax.domain.transfer.controller;
import dev.syntax.domain.transfer.dto.AutoTransferReq;
import dev.syntax.domain.transfer.dto.AutoTransferRes;
import dev.syntax.domain.transfer.service.AutoTransferCreateService;
import dev.syntax.domain.transfer.service.AutoTransferInquiryService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 자동이체 관련 API 컨트롤러.
 * <p>
 * 자동이체 설정 생성, 조회, 수정, 삭제 등의 기능을 제공합니다.
 * </p>
 */
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AutoTransferController {

    private final AutoTransferInquiryService autoTransferInquiryService;
    private final AutoTransferCreateService autoTransferCreateService;
    
    /**
     * 자동이체 설정 조회 API.
     * <p>
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

        AutoTransferRes res = autoTransferInquiryService.getAutoTransfer(id, ctx);
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
