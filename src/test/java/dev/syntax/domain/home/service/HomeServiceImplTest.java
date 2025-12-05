package dev.syntax.domain.home.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreChildAccountInfoRes;
import dev.syntax.domain.account.dto.core.CoreUserAccountListRes;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.home.dto.HomeRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HomeServiceImpl에 대한 단위 테스트
 * UserContext를 통해 받은 사용자 정보와 CoreClient로부터 받은 계좌 정보를 기반으로
 * 홈 화면 데이터를 올바르게 가공하는지 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class HomeServiceImplTest {

    @InjectMocks
    private HomeServiceImpl homeService;

    @Mock
    private CoreAccountClient coreAccountClient;

    // UserContext에 필요한 필드 Mock Data
    private final Long PARENT_ID = 1L;
    private final String PARENT_NAME = "부모님";
    private final String PARENT_EMAIL = "parent@test.com";
    private final BigDecimal PARENT_ACCOUNT_BALANCE = new BigDecimal("500000");

    private UserContext parentContextNoChildren;
    private User mockParent;

    // Core DTO Mock (실제 DTO 구조를 알 수 없으므로, 테스트에 필요한 데이터만 포함하여 정의)

    // 참고: HomeServiceImpl의 sumAccountBalances 함수는 AccountItemRes.balance를 사용합니다.
    private CoreAccountItemRes createAccountItemRes(BigDecimal balance, AccountType type) {
        // 실제 CoreAccountItemRes DTO가 가진 필드 순서와 타입을 가정합니다.
        return new CoreAccountItemRes(1L, "1234", type, balance);
    }

    // Core 서버 응답 Mock: 자녀 계좌 정보가 없는 부모의 응답 (parentBalance만 포함)
    private CoreUserAccountListRes createParentCoreResponse() {
        // 부모 본인의 계좌 목록 (예: 예금 계좌)
        List<CoreAccountItemRes> parentAccounts = List.of(
                createAccountItemRes(PARENT_ACCOUNT_BALANCE, AccountType.DEPOSIT)
        );
        // 자녀 계좌 정보는 빈 리스트
        List<CoreChildAccountInfoRes> noChildrenAccounts = Collections.emptyList();

        return new CoreUserAccountListRes(parentAccounts, noChildrenAccounts);
    }

    @BeforeEach
    void setUp() {
        // 1. Mock Parent User 설정
        mockParent = mock(User.class);
        when(mockParent.getId()).thenReturn(PARENT_ID);
        when(mockParent.getName()).thenReturn(PARENT_NAME);
        when(mockParent.getEmail()).thenReturn(PARENT_EMAIL);
        when(mockParent.getRole()).thenReturn(Role.PARENT);
        // 부모의 자녀 리스트는 비어 있음 (가족 연결 없음)
        // HomeServiceImpl 로직에서 이 User의 getChildren()을 사용하므로 이 Mocking은 필요함.
        when(mockParent.getChildren()).thenReturn(Collections.emptyList());

        // 2. Mock Parent UserContext 설정 (자녀 목록 비어있음)
        parentContextNoChildren = mock(UserContext.class);
        when(parentContextNoChildren.getUser()).thenReturn(mockParent);
        // UserContext의 getChildren()은 HomeServiceImpl에서 직접 사용되지 않으므로, UnnecessaryStubbingException을 피하기 위해 제거합니다.
        // when(parentContextNoChildren.getChildren()).thenReturn(Collections.emptyList());

        // 3. CoreClient Mock 설정 (Core 서버 응답)
        when(coreAccountClient.getUserAccounts()).thenReturn(createParentCoreResponse());

        // 주의: HomeServiceImpl에서 NumberFormattingService(BigDecimal)를 사용합니다.
        // 이 테스트에서는 해당 유틸리티가 "500,000"과 같이 쉼표를 포함한 문자열을 반환한다고 가정합니다.
    }

    // ----------------------------------------------------------------------------------
    // TC-HOME-003: 가족 연결 없는 사용자 (부모 역할) 홈 조회
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-HOME-003: 가족 연결이 없는 부모 사용자가 홈 화면 조회 시 본인 잔액만 표시되고 자녀 목록은 비어 있어야 한다.")
    void getHomeData_ParentWithNoChildren_ReturnsEmptyChildList() {
        // GIVEN: setUp에서 PARENT 역할이며 자녀 연결이 없는 parentContextNoChildren 설정 완료

        // WHEN
        HomeRes result = homeService.getHomeData(parentContextNoChildren);

        // THEN
        // 1. CoreClient 호출 검증 (1회)
        verify(coreAccountClient, times(1)).getUserAccounts();

        // 2. UserDto 검증
        HomeRes.UserDto userDto = result.user();
        assertNotNull(userDto);
        assertEquals(PARENT_ID, userDto.userId());
        assertEquals(Role.PARENT, userDto.role());

        // 3. 잔액 검증
        // 500000 -> "500,000" (NumberFormattingService가 쉼표를 넣는다고 가정)
        assertEquals("500,000", userDto.balance(), "부모 본인의 잔액이 포맷팅되어 반환되어야 합니다.");

        // 4. 자녀 목록 검증 (핵심)
        assertNotNull(userDto.children());
        assertTrue(userDto.children().isEmpty(), "가족 연결이 없으므로 자녀 목록은 비어 있어야 합니다.");
    }

    // ----------------------------------------------------------------------------------
    // TBD: 자녀 사용자의 홈 화면 조회 테스트 (추가 필요 시)
    // ----------------------------------------------------------------------------------
}