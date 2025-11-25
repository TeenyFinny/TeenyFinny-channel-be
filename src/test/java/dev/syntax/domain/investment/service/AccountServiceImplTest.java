package dev.syntax.domain.investment.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 투자계좌_생성_성공() {
        Long userId = 1L;
        String cano = "12345678";

        // 1. 코어 서버 호출 모킹
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("cano", cano)));

        // 2. 사용자 조회 모킹
        User user = User.builder()
                .id(userId)
                .name("테스트유저")
                .role(Role.CHILD) // 또는 PARENT, 상황에 맞게
                .children(new ArrayList<>()) // 필요하면 관계 추가
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // 3. 저장 모킹
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 실행
        var res = accountService.createInvestmentAccount(userId);

        // 검증
        assertNotNull(res);
        assertEquals(cano, res.getCano());

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void 투자계좌_생성_실패_코어() {
        Long userId = 1L;

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> accountService.createInvestmentAccount(userId));

        assertEquals(ErrorBaseCode.CREATE_FAILED, exception.getErrorCode());
    }

    @Test
    void 투자계좌_생성_실패_유저없음() {
        Long userId = 1L;

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("cano", "12345678")));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> accountService.createInvestmentAccount(userId));

        assertEquals(ErrorBaseCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}
