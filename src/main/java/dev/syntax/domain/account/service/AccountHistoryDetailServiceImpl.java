package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountHistoryDetailRes;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 계좌 거래내역 상세 조회 서비스 구현체.
 *
 * <p>
 * 현재는 Mock 데이터를 사용하여 거래 상세 정보를 반환합니다.
 * 추후 CoreBankClient를 통해 실제 데이터를 조회하도록 변경될 예정입니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountHistoryDetailServiceImpl implements AccountHistoryDetailService {

    /**
     * {@inheritDoc}
     * <p>
     * Mock 데이터를 기반으로 상세 정보를 반환합니다.
     * </p>
     */
    @Override
    public AccountHistoryDetailRes getDetail(Long transactionId, UserContext ctx) {

        log.info("거래 상세 조회 요청 transactionId={}", transactionId);

        // 🔹 실제 구현에서는 CoreBankClient 로 호출해야 함
        // 🔹 현재는 Mock 데이터로 대체
        return mockCoreDetail(transactionId);
    }

    /**
     * 코어 서버 Mock 상세 응답
     */
    private AccountHistoryDetailRes mockCoreDetail(Long transactionId) {

        // transactionId에 따라 mock 분기 가능
        if (transactionId.equals(202501150001L)) {
            return new AccountHistoryDetailRes(
                    "이체",
                    "50,000",
                    "2025-01-15 13:22",
                    "일시불",
                    "이체",
                    "50,000",
                    "150,000");
        }

        if (transactionId.equals(202501150002L)) {
            return new AccountHistoryDetailRes(
                    "편의점",
                    "1,500",
                    "2025-01-15 14:10",
                    "일시불",
                    "식비",
                    "1,500",
                    "148,500");
        }

        if (transactionId.equals(202501160001L)) {
            return new AccountHistoryDetailRes(
                    "스타벅스",
                    "5,300",
                    "2025-01-16 10:23",
                    "할부",
                    "카페/간식",
                    "5,300",
                    "143,200");
        }

        throw new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY);
    }
}
