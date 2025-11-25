package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreInvestmentAccountRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.core.CoreApiProperties;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

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
                "http://localhost:8081/core/banking/account/investment?userId=" + userId,
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
                "http://localhost:8081/core/banking/account/investment?userId=" + userId,
                null,
                CoreInvestmentAccountRes.class
        )).thenReturn(null);

        CoreInvestmentAccountRes res = coreAccountClient.createInvestmentAccount(userId);

        assertNull(res);
    }
}
