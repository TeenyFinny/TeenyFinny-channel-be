package dev.syntax.domain.admin.controller;

import dev.syntax.domain.admin.dto.AdminAutoTransferRes;
import dev.syntax.domain.admin.dto.AdminFailedTransactionRes;
import dev.syntax.domain.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 관리자 API 컨트롤러
 * <p>
 * 관리자만 접근 가능한 자동이체 관리 및 실패 거래 조회 API를 제공합니다.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * 자동이체 목록 조회
     *
     * @param status    필터링할 상태 (선택사항: PROCESSING, SUCCESS, FAIL)
     * @param startDate 시작 날짜 (선택사항)
     * @param endDate   종료 날짜 (선택사항)
     * @param page      페이지 번호 (기본값: 0)
     * @param size      페이지 크기 (기본값: 20)
     * @return 자동이체 목록 (페이징)
     */
    @GetMapping("/auto-transfer")
    @ResponseStatus(HttpStatus.OK)
    public Page<AdminAutoTransferRes> getAutoTransfers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("[관리자 API] 자동이체 목록 조회 - status: {}, startDate: {}, endDate: {}, page: {}, size: {}",
                status, startDate, endDate, page, size);

        return adminService.getAutoTransferList(status, startDate, endDate, page, size);
    }

    /**
     * 자동이체 수동 실행
     *
     * @param autoTransferId 실행할 자동이체 ID
     */
    @PostMapping("/auto-transfer/{autoTransferId}/execute")
    @ResponseStatus(HttpStatus.OK)
    public void executeAutoTransfer(@PathVariable Long autoTransferId) {
        log.info("[관리자 API] 자동이체 수동 실행 - autoTransferId: {}", autoTransferId);
        adminService.executeAutoTransfer(autoTransferId);
    }

    /**
     * 실패한 거래 목록 조회
     *
     * @param autoTransferOnly true일 경우 자동이체 관련 실패 거래만 조회 (기본값: false)
     * @param page             페이지 번호 (기본값: 0)
     * @param size             페이지 크기 (기본값: 20)
     * @return 실패한 거래 목록 (페이징)
     */
    @GetMapping("/transaction/failed")
    @ResponseStatus(HttpStatus.OK)
    public Page<AdminFailedTransactionRes> getFailedTransactions(
            @RequestParam(defaultValue = "false") boolean autoTransferOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("[관리자 API] 실패 거래 조회 - autoTransferOnly: {}, page: {}, size: {}",
                autoTransferOnly, page, size);

        return adminService.getFailedTransactions(autoTransferOnly, page, size);
    }
}
