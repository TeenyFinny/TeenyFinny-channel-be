package dev.syntax.domain.report.mock;

import dev.syntax.domain.report.dto.CoreTransactionRes;
import dev.syntax.domain.report.enums.Category;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MockCoreBankClient {

    public List<CoreTransactionRes> getMonthlyHistory(Long userId, int month) {

        // TODO: 이후 실제 CoreBankClient 붙이면 이 mock은 삭제하면 됨.

        // 예시: 특정 월에는 데이터 없음 처리
        if (month == 1) {
            return List.of();
        }

        // 더미 거래내역
        return List.of(
                new CoreTransactionRes(Category.TRANSFER, new BigDecimal("328830")),
                new CoreTransactionRes(Category.ETC, new BigDecimal("126430")),
                new CoreTransactionRes(Category.SHOPPING, new BigDecimal("88440")),
                new CoreTransactionRes(Category.ETC, new BigDecimal("6257"))
        );
    }
}
