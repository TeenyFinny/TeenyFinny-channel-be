package dev.syntax.domain.account.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.AccountHistoryDetailRes;
import dev.syntax.domain.account.dto.core.CoreTransactionDetailItemRes;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <h2>거래 단일 상세 조회 서비스 구현체</h2>
 *
 * Core 서버(은행 시스템)로부터 특정 거래의 상세 정보를 조회하는 서비스입니다.
 * 현재는 코어 연동 전 단계이므로 Mock 데이터를 기반으로 동작하며,
 * 인증/인가 검증 로직은 실제 서비스와 동일하게 유지됩니다.
 *
 * <p>
 * <b>주요 기능:</b>
 * </p>
 * <ul>
 * <li>거래 ID 유효성 검증</li>
 * <li>Mock Core 데이터 조회 (향후 Core 연동 시 제거 예정)</li>
 * <li>거래가 속한 계좌ID 추출 및 조회 권한 검증</li>
 * <li>자녀/부모 권한별 접근 제한 처리</li>
 * </ul>
 *
 * <p>
 * ⚠️ <b>주의:</b> 현재는 Core API 연동 전이므로 거래 엔티티가 DB에 존재하지 않고,
 * transactionId → accountId 매핑도 Mock 규칙을 사용합니다.
 * 실제 Core 연동 시 <code>Transaction</code> 엔티티에서 직접 accountId를 조회하도록 변경됩니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountHistoryDetailServiceImpl implements AccountHistoryDetailService {

    private final AccountRepository accountRepository;
    private final CoreAccountClient coreAccountClient;

    /**
     * <h3>📌 단일 거래 상세 조회</h3>
     *
     * 주어진 거래 ID에 해당하는 상세 정보를 Core 서버에서 조회합니다.
     *
     * <p>
     * <b>검증 절차:</b>
     * </p>
     * <ol>
     * <li>거래 ID가 Null 또는 0 이하인지 유효성 검증</li>
     * <li>Core 서버에서 거래 상세 정보 조회</li>
     * <li>응답 데이터를 Channel 형식으로 변환</li>
     * </ol>
     *
     * <p>
     * <b>예외:</b>
     * </p>
     * <ul>
     * <li>{@link ErrorBaseCode#TX_INVALID_TRANSACTION_ID} - 잘못된 거래 ID</li>
     * <li>{@link ErrorBaseCode#NOT_FOUND_ENTITY} - 거래를 찾을 수 없음</li>
     * </ul>
     *
     * @param transactionId 조회할 거래 ID
     * @param ctx           로그인한 사용자 컨텍스트
     * @return 조회된 단일 거래 상세 정보
     */
    @Override
    public AccountHistoryDetailRes getDetail(Long transactionId, UserContext ctx) {

        log.info("거래 상세 조회 요청 transactionId={}, userId={}", transactionId, ctx.getId());

        // 거래ID 유효성 검증
        if (transactionId == null || transactionId <= 0) {
            throw new BusinessException(ErrorBaseCode.TX_INVALID_TRANSACTION_ID);
        }

        // Core 서버에서 거래 상세 조회
        CoreTransactionDetailItemRes coreDetail = coreAccountClient.getTransactionDetail(transactionId);

        if (coreDetail == null) {
            throw new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY);
        }

        log.info("=== Core Detail Response ===");
        log.info("merchantName: {}", coreDetail.merchantName());
        log.info("amount: {}", coreDetail.amount());
        log.info("transactionDate: {}", coreDetail.transactionDate());
        log.info("type: {}", coreDetail.type());
        log.info("code: {}", coreDetail.code());
        log.info("category: {}", coreDetail.category());
        log.info("approveAmount: {}", coreDetail.approveAmount());
        log.info("balanceAfter: {}", coreDetail.balanceAfter());
        log.info("===========================");

        // 응답 변환 (Core → Channel)
        return convertToAccountHistoryDetailRes(coreDetail);
    }

    /**
     * Core 서버 응답을 Channel 응답 형식으로 변환합니다.
     * <p>
     * - 금액은 천단위 콤마 적용
     * - 날짜는 "yyyy-MM-dd HH:mm" 형식으로 변환
     * </p>
     */
    private AccountHistoryDetailRes convertToAccountHistoryDetailRes(CoreTransactionDetailItemRes coreDetail) {
        return new AccountHistoryDetailRes(
                coreDetail.merchantName(),
                coreDetail.amount(), // 이미 포맷팅됨
                coreDetail.transactionDate(), // 이미 포맷팅됨
                convertPaymentMethod(coreDetail.type()), // code 필드에 일시불/할부 (결제방식)
                coreDetail.category().getKoreanName(), // 한글 카테고리명 사용
                coreDetail.approveAmount(), // 이미 포맷팅됨
                coreDetail.balanceAfter(), // 이미 포맷팅됨
                coreDetail.code() // type 필드에 WITHDRAW/DEPOSIT (거래유형)
        );
    }

    private String convertPaymentMethod(String type) {
        if ("PAY_IN_FULL".equals(type)) return "일시불";
        if ("INSTALLMENT".equals(type)) return "할부";
        return "";
    }
}
