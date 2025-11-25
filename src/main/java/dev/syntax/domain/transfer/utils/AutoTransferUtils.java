package dev.syntax.domain.transfer.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 자동이체 관련 유틸리티 클래스.
 */
public class AutoTransferUtils {

    private AutoTransferUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 투자 금액을 계산합니다.
     * <p>
     * 총 금액 × (투자 비율 / 100)
     * </p>
     *
     * @param totalAmount 총 금액
     * @param ratio 투자 비율 (0~100)
     * @return 투자 금액
     */
    public static BigDecimal calculateInvestAmount(BigDecimal totalAmount, int ratio) {
        return totalAmount
                .multiply(BigDecimal.valueOf(ratio))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP); // 반올림 처리
    }

    /**
     * 용돈 금액을 계산합니다.
     * <p>
     * 총 금액 - 투자 금액
     * </p>
     *
     * @param totalAmount 총 금액
     * @param investAmount 투자 금액
     * @return 용돈 금액
     */
    public static BigDecimal calculateAllowanceAmount(BigDecimal totalAmount, BigDecimal investAmount) {
        return totalAmount.subtract(investAmount);
    }

    /**
     * 총 금액과 비율로 용돈/투자 금액을 한 번에 계산합니다.
     *
     * @param totalAmount 총 금액
     * @param ratio 투자 비율 (0~100)
     * @return [용돈 금액, 투자 금액] 배열
     */
    public static BigDecimal[] calculateAmounts(BigDecimal totalAmount, int ratio) {
        BigDecimal investAmount = calculateInvestAmount(totalAmount, ratio);
        BigDecimal allowanceAmount = calculateAllowanceAmount(totalAmount, investAmount);
        return new BigDecimal[]{allowanceAmount, investAmount};
    }
}
