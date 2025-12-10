package dev.syntax.domain.admin.service;

import dev.syntax.domain.admin.dto.AdminAutoTransferRes;
import dev.syntax.domain.admin.dto.AdminFailedTransactionRes;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

/**
 * 관리자 기능 서비스 인터페이스
 */
public interface AdminService {

    /**
     * 자동이체 목록 조회
     *
     * @param status    필터링할 상태 (선택사항)
     * @param startDate 시작 날짜 (선택사항)
     * @param endDate   종료 날짜 (선택사항)
     * @param page      페이지 번호
     * @param size      페이지 크기
     * @return 자동이체 목록 (페이징)
     */
    Page<AdminAutoTransferRes> getAutoTransferList(
            String status,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );

    /**
     * 자동이체 수동 실행
     *
     * @param autoTransferId 실행할 자동이체 ID
     */
    void executeAutoTransfer(Long autoTransferId);

    /**
     * 실패한 거래 목록 조회
     *
     * @param autoTransferOnly true일 경우 자동이체 관련 실패 거래만 조회
     * @param page             페이지 번호
     * @param size             페이지 크기
     * @return 실패한 거래 목록 (페이징)
     */
    Page<AdminFailedTransactionRes> getFailedTransactions(
            boolean autoTransferOnly,
            int page,
            int size
    );
}
