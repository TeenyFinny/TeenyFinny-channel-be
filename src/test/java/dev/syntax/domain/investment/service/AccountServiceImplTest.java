package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreInvestmentAccountRes;
import dev.syntax.global.core.CoreApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    @InjectMocks
    private CoreAccountClient coreAccountClient;

    @Mock
    private RestTemplate coreRestTemplate;

    @Mock
    private CoreApiProperties properties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(properties.getBaseUrl()).thenReturn("http://localhost:8081");
    }

    @Test
    void 투자계좌생성_성공() {
        Long userId = 1L;
        CoreInvestmentAccountRes mockRes = new CoreInvestmentAccountRes("12345678",1L,new BigDecimal(0));

        when(coreRestTemplate.postForObject(
                properties.getBaseUrl() + "/core/banking/account/investment?userId=" + userId,
                null,
                CoreInvestmentAccountRes.class
        )).thenReturn(mockRes);

        CoreInvestmentAccountRes res = coreAccountClient.createInvestmentAccount(userId);

        assertNotNull(res);
        assertEquals("12345678", res.getAccountNumber());
    }

    @Test
    void 투자계좌생성_실패_null() {
        Long userId = 1L;

        when(coreRestTemplate.postForObject(
                properties.getBaseUrl() + "/core/banking/account/investment?userId=" + userId,
                null,
                CoreInvestmentAccountRes.class
        )).thenReturn(null);

        CoreInvestmentAccountRes res = coreAccountClient.createInvestmentAccount(userId);

        assertNull(res);
    }
}
