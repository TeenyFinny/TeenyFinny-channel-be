package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.client.CoreInvestmentClient;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
// 실제 InvestDashboardRes DTO를 import 합니다.
import dev.syntax.domain.investment.dto.res.InvestDashboardRes;
import dev.syntax.domain.investment.dto.HoldingItem; // HoldingItem DTO 필요 시 import

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestDashboardServiceImplTest {

    // 테스트 대상 서비스
    @InjectMocks
    private InvestDashboardServiceImpl investDashboardService;

    // 의존성 Mock
    @Mock
    private CoreInvestmentClient coreInvestmentClient;

    @Mock
    private InvestAccountService accountService;

    // Test Data
    private User childUser; // UserContext 생성에 사용될 User 객체
    private UserContext userContext;
    private final Long USER_ID = 1L;
    private final String TEST_CANO = "5801234567";
    private InvestDashboardRes expectedDashboardRes; // 실제 DTO 타입 사용

    // Mock Data for DTO
    private final String MOCK_DEPOSIT_AMT = "1,000,000";
    private final String MOCK_TOT_EVLU_AMT = "1,500,000";
    private final String MOCK_TOTAL_PROFIT_AMT = "500,000";
    private final Double MOCK_TOTAL_PROFIT_RATE = 50.0;

    @BeforeEach
    void setUp() {
        // 1. User 엔티티 설정 (UserContext의 유일한 생성자 요구사항 충족)
        childUser = User.builder()
                .id(USER_ID)
                .name("TestChild")
                .role(Role.CHILD)
                .build();

        // 2. UserContext 설정 (User 객체를 인자로 전달)
        userContext = new UserContext(childUser);

        // 3. Mock 응답 DTO 설정 (서비스 로직이 의존하는 필드만 스터빙)
        expectedDashboardRes = mock(InvestDashboardRes.class);

        // DTO getter 호출은 검증 단계(THEN)에서만 사용되므로 lenient()를 적용하여
        // UnnecessaryStubbingException을 회피합니다.
        lenient().when(expectedDashboardRes.depositAmount()).thenReturn(MOCK_DEPOSIT_AMT);
        lenient().when(expectedDashboardRes.totEvluAmt()).thenReturn(MOCK_TOT_EVLU_AMT);
        lenient().when(expectedDashboardRes.totalProfitAmount()).thenReturn(MOCK_TOTAL_PROFIT_AMT);
        lenient().when(expectedDashboardRes.totalProfitRate()).thenReturn(MOCK_TOTAL_PROFIT_RATE);
        lenient().when(expectedDashboardRes.top3Holdings()).thenReturn(Collections.emptyList());

        // setUp()에 있던 when(expectedDashboardRes.userId()).thenReturn(USER_ID);
        // 스터빙은 UnnecessaryStubbingException을 일으키므로 제거되었습니다.
    }

    // ----------------------------------------------------------------------------------
    // 1. TC-INVEST-003: 투자 대시보드 조회 (getDashboard 성공)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-INVEST-003: 투자 대시보드 조회 성공")
    void getDashboard_Success() {
        // GIVEN
        Long userIdToQuery = userContext.getId();

        // userId 필드 스터빙은 테스트 메서드 내에서 필요한 경우에만 수행하며, lenient() 적용
        lenient().when(expectedDashboardRes.userId()).thenReturn(USER_ID);

        // 1. accountService.getCanoByUserId() 호출 시 TEST_CANO 반환 Mocking
        when(accountService.getCanoByUserId(userIdToQuery)).thenReturn(TEST_CANO);

        // 2. coreInvestmentClient.getDashboard() 호출 시 Mock 응답 DTO 반환 Mocking
        when(coreInvestmentClient.getDashboard(TEST_CANO)).thenReturn(expectedDashboardRes);

        // WHEN
        InvestDashboardRes actualDashboardRes = investDashboardService.getDashboard(userContext);

        // THEN
        // 1. 결과 검증 (실제 DTO의 필드와 getter에 맞게 검증 로직 수정)
        assertNotNull(actualDashboardRes);
        assertEquals(expectedDashboardRes.userId(), actualDashboardRes.userId());
        assertEquals(expectedDashboardRes.depositAmount(), actualDashboardRes.depositAmount());
        assertEquals(expectedDashboardRes.totEvluAmt(), actualDashboardRes.totEvluAmt());
        assertEquals(expectedDashboardRes.totalProfitAmount(), actualDashboardRes.totalProfitAmount());
        assertEquals(expectedDashboardRes.totalProfitRate(), actualDashboardRes.totalProfitRate());

        // 2. 메서드 호출 검증
        // accountService가 올바른 userId로 호출되었는지 검증
        verify(accountService, times(1)).getCanoByUserId(eq(userIdToQuery));

        // coreInvestmentClient가 accountService에서 반환된 cano로 호출되었는지 검증
        verify(coreInvestmentClient, times(1)).getDashboard(eq(TEST_CANO));
    }
}