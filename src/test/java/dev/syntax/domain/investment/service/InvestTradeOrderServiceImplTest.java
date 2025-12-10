package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.domain.investment.dto.core.CoreInvestTradeOrderReq;
import dev.syntax.domain.investment.dto.req.InvestTradeOrderReq;
import dev.syntax.domain.investment.dto.res.InvestTradeOrderRes;
import dev.syntax.domain.investment.enums.TradeType; // 실제 Enum import (가정)
import dev.syntax.domain.investment.enums.OrderStatus; // 실제 Enum import (가정)
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestTradeOrderServiceImplTest {

    @InjectMocks
    private InvestTradeOrderServiceImpl investTradeOrderService;

    @Mock
    private CoreInvestmentClient coreInvestmentClient;

    // 테스트에 사용될 Mock Data
    private final String CANO = "1234567890"; // 계좌 번호
    private final String PRODUCT_CODE = "P001";
    private final String PRODUCT_NAME = "테스트 주식";
    private final int QUANTITY_INT = 10; // InvestTradeOrderReq/CoreReq의 quantity (int)
    private final long QUANTITY_LONG = 10L; // InvestTradeOrderRes의 quantity (long)
    private final String PRICE = "50000"; // InvestTradeOrderReq/CoreReq/Res의 price (String)

    private InvestTradeOrderReq mockReq;
    
    @BeforeEach
    void setUp() {
        // 1. 요청 DTO Mock (InvestTradeOrderReq): 모든 테스트에서 공통으로 사용되므로 setUp에 유지
        mockReq = mock(InvestTradeOrderReq.class);
        when(mockReq.getProductCode()).thenReturn(PRODUCT_CODE);
        when(mockReq.getProductName()).thenReturn(PRODUCT_NAME);
        when(mockReq.getQuantity()).thenReturn(QUANTITY_INT); // int 타입으로 Stubbing
        when(mockReq.getPrice()).thenReturn(PRICE); // String 타입으로 Stubbing

        // 2. 응답 DTO Mock (InvestTradeOrderRes):
        // mockBuyRes와 mockSellRes에 대한 Stubbing은 불필요한 Stubbing 오류를 피하기 위해
        // 각 테스트 메서드 내부에서 필요한 것만 정의하도록 여기서 제거합니다.

        // mockRes는 각 테스트 메서드에서 재정의될 것입니다.
    }

    /**
     * 특정 거래 유형(TradeType)에 맞는 Mock 응답 DTO를 생성합니다.
     * @param tradeType 응답에 설정할 거래 유형
     * @return Stubbing이 완료된 InvestTradeOrderRes 객체
     */
    private InvestTradeOrderRes createMockRes(TradeType tradeType) {
        InvestTradeOrderRes res = mock(InvestTradeOrderRes.class);
        when(res.getTradeType()).thenReturn(tradeType);
        when(res.getProductCode()).thenReturn(PRODUCT_CODE);
        when(res.getProductName()).thenReturn(PRODUCT_NAME);
        when(res.getQuantity()).thenReturn(QUANTITY_LONG);
        when(res.getPrice()).thenReturn(PRICE);
        when(res.getStatus()).thenReturn(OrderStatus.EXECUTED);
        return res;
    }

    // ----------------------------------------------------------------------------------
    // TC-INVEST-007: 정상 매수 주문 (Buy)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-INVEST-007: 정상적인 매수 주문 요청이 Core 클라이언트에게 올바르게 전달되고 응답을 반환한다.")
    void buy_Success() {
        // GIVEN
        // 1. 매수 성공 응답 Mock 생성 및 설정 (TC-INVEST-007에만 필요)
        InvestTradeOrderRes mockBuyRes = createMockRes(TradeType.TTTTC0012U);

        // 2. Core 클라이언트의 매수 API 호출 Mocking
        when(coreInvestmentClient.tradeOrderBuy(any(CoreInvestTradeOrderReq.class)))
                .thenReturn(mockBuyRes);

        // WHEN
        InvestTradeOrderRes result = investTradeOrderService.buy(CANO, mockReq);

        // THEN
        // 1. Core 클라이언트의 tradeOrderBuy가 1번 호출되었는지 검증
        ArgumentCaptor<CoreInvestTradeOrderReq> captor = ArgumentCaptor.forClass(CoreInvestTradeOrderReq.class);
        verify(coreInvestmentClient, times(1)).tradeOrderBuy(captor.capture());
        verify(coreInvestmentClient, never()).tradeOrderSell(any()); // Sell은 호출되지 않아야 함

        // 2. CoreInvestTradeOrderReq의 인수가 올바르게 매핑되었는지 검증 (Core DTO 검증)
        CoreInvestTradeOrderReq capturedReq = captor.getValue();
        assertEquals(CANO, capturedReq.getCano());
        assertEquals(PRODUCT_CODE, capturedReq.getProductCode());
        assertEquals(PRODUCT_NAME, capturedReq.getProductName());
        assertEquals(QUANTITY_INT, capturedReq.getQuantity());
        assertEquals(PRICE, capturedReq.getPrice());

        // 3. 반환된 DTO가 Core API의 응답과 동일한지, 그리고 새로운 필드들이 올바른지 검증
        assertNotNull(result);
        // Enum 값 검증: TTTTC0012U
        assertEquals(TradeType.TTTTC0012U, result.getTradeType());
        assertEquals(mockBuyRes.getProductCode(), result.getProductCode());
        assertEquals(mockBuyRes.getProductName(), result.getProductName());
        assertEquals(mockBuyRes.getQuantity(), result.getQuantity()); // long 타입
        assertEquals(mockBuyRes.getPrice(), result.getPrice());
        // Enum 값 검증: EXECUTED
        assertEquals(OrderStatus.EXECUTED, result.getStatus());
    }

    // ----------------------------------------------------------------------------------
    // TC-INVEST-008: 정상 매도 주문 (Sell)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-INVEST-008: 정상적인 매도 주문 요청이 Core 클라이언트에게 올바르게 전달되고 응답을 반환한다.")
    void sell_Success() {
        // GIVEN
        // 1. 매도 성공 응답 Mock 생성 및 설정 (TC-INVEST-008에만 필요)
        InvestTradeOrderRes mockSellRes = createMockRes(TradeType.TTTCO011U);

        // 2. Core 클라이언트의 매도 API 호출 Mocking
        when(coreInvestmentClient.tradeOrderSell(any(CoreInvestTradeOrderReq.class)))
                .thenReturn(mockSellRes);

        // WHEN
        InvestTradeOrderRes result = investTradeOrderService.sell(CANO, mockReq);

        // THEN
        // 1. Core 클라이언트의 tradeOrderSell이 1번 호출되었는지 검증
        ArgumentCaptor<CoreInvestTradeOrderReq> captor = ArgumentCaptor.forClass(CoreInvestTradeOrderReq.class);
        verify(coreInvestmentClient, times(1)).tradeOrderSell(captor.capture());
        verify(coreInvestmentClient, never()).tradeOrderBuy(any()); // Buy는 호출되지 않아야 함

        // 2. CoreInvestTradeOrderReq의 인수가 올바르게 매핑되었는지 검증 (Core DTO 검증)
        CoreInvestTradeOrderReq capturedReq = captor.getValue();
        assertEquals(CANO, capturedReq.getCano());
        assertEquals(PRODUCT_CODE, capturedReq.getProductCode());
        assertEquals(PRODUCT_NAME, capturedReq.getProductName()); // productName도 Core Req에 포함되어야 함
        assertEquals(QUANTITY_INT, capturedReq.getQuantity());
        assertEquals(PRICE, capturedReq.getPrice());

        // 3. 반환된 DTO가 Core API의 응답과 동일한지, 그리고 새로운 필드들이 올바른지 검증
        assertNotNull(result);
        // Enum 값 검증: TTTCO011U
        assertEquals(TradeType.TTTCO011U, result.getTradeType());
        assertEquals(mockSellRes.getProductCode(), result.getProductCode());
        assertEquals(mockSellRes.getProductName(), result.getProductName());
        assertEquals(mockSellRes.getQuantity(), result.getQuantity()); // long 타입
        assertEquals(mockSellRes.getPrice(), result.getPrice());
        // Enum 값 검증: EXECUTED
        assertEquals(OrderStatus.EXECUTED, result.getStatus());
    }
}