package dev.syntax.domain.investment.dto;

/**
 * 사용자의 보유 종목 정보를 나타내는 DTO입니다.
 * <p>
 * Portfolio(보유 데이터)와 KIS API로부터 가져온 실시간/스냅샷 시세 데이터를 결합한 결과를 표현합니다.
 * 포트폴리오 조회 API에서 개별 종목 단위로 계산된 정보를 담습니다.
 *
 * <h3>구성 요소</h3>
 * <ul>
 *     <li>종목 정보: 종목코드(productCode), 종목명(productName)</li>
 *     <li>보유 정보: 보유 수량(quantity), 평균 매입단가(avgPrice)</li>
 *     <li>시세 정보: 현재가(currentPrice)</li>
 *     <li>평가 정보: 평가금액(evaluationAmount), 평가손익(profitAmount), 수익률(profitRate)</li>
 *     <li>비중 정보: 전체 포트폴리오 대비 보유 종목 비중(weight)</li>
 * </ul>
 *
 * <h3>계산 공식</h3>
 * <ul>
 *     <li><b>평가금액(evaluationAmount)</b> = currentPrice × quantity</li>
 *     <li><b>평가손익(profitAmount)</b> = evaluationAmount − (avgPrice × quantity)</li>
 *     <li><b>수익률(profitRate)</b> = profitAmount / (avgPrice × quantity)</li>
 *     <li><b>비중(weight)</b> = evaluationAmount / 총 포트폴리오 평가금액</li>
 * </ul>
 */
public record HoldingItem(
        String productCode,   // 종목코드 (e.g., 005930)
        String productName,   // 종목명 (e.g., 삼성전자)
        String quantity,        // 보유 수량
        String avgPrice,        // 평균 매입 단가 (Portfolio 저장 값)
        String currentPrice,    // 현재가 (KIS 실시간/스냅샷 데이터)
        String evaluationAmount, // 평가금액 = 현재가 * 보유수량
        String profitAmount,     // 평가손익 = 평가금액 - (평균매입단가 * 보유수량)
        Double profitRate,     // 수익률 (%) = 평가손익 / (평균매입단가 * 보유수량)
        Double weight          // 전체 포트폴리오에서 해당 종목이 차지하는 비중 (0~1)
) {}

