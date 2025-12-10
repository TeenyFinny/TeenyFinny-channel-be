package dev.syntax.domain.account.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.AccountHistoryReq;
import dev.syntax.domain.account.dto.AccountHistoryRes;
import dev.syntax.domain.account.dto.core.CoreTransactionHistoryRes;
import dev.syntax.domain.account.dto.core.CoreTransactionItemRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.report.enums.Category; // 실제 Category 타입 import
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import dev.syntax.global.service.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate; // LocalDate import 추가
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountHistoryServiceImplTest {

    // AccountHistoryRes의 timestamp 필드 포맷
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @InjectMocks
    private AccountHistoryServiceImpl accountHistoryService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CoreAccountClient coreAccountClient;

    // Mock DTO 및 Entity 정의 (테스트 내부에서만 사용)

    // 거래 내역 카테고리 (Core DTO에 포함됨) - 실제 Category enum을 사용하므로 내부 정의 제거

    // 테스트 데이터
    private final Long CHILD_USER_ID = 100L;
    private final String ACCOUNT_NO = "1234567890";
    private final BigDecimal FINAL_BALANCE = new BigDecimal("145000"); // 최종 잔액
    private UserContext childContext;
    private AccountHistoryReq historyReq;
    private Account allowanceAccount;
    private CoreTransactionHistoryRes coreRes;
    private User mockChildUser; // User 엔티티 추가

    // DTO의 LocalDate 필드를 String으로 변환한 값 (CoreClient Mocking에 사용)
    private LocalDate coreClientStartDate;
    private LocalDate coreClientEndDate;

    // Helper method to simulate Category's Korean name getter for testing purposes
    // NOTE: 실제 Category Enum이 getKoreanName() 메서드를 제공한다고 가정합니다.
    private String getKoreanName(Category category) {
        return switch (category) {
            case FOOD -> "식비";
            case SHOPPING -> null;
            case EDU -> null;
            case TRANSPORT -> "교통";
            case TRANSFER -> "이체";
            case ETC -> "기타";
            case ENT -> null;
        };
    }

    @BeforeEach
    void setUp() {
        // 1. User 엔티티 Mocking (Account 엔티티 생성을 위해 필요)
        mockChildUser = User.builder()
                .id(CHILD_USER_ID)
                .name("ChildName")
                .role(Role.CHILD)
                .build();

        // 2. UserContext (자녀 역할) 설정 - userId는 동일해야 함
        childContext = new UserContext(mockChildUser);

        // 3. AccountHistoryReq 설정 (기간)
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 12, 31);
        historyReq = new AccountHistoryReq(startDate, endDate);

        // AccountHistoryServiceImpl이 CoreAccountClient를 호출할 때 
        coreClientStartDate = startDate;
        coreClientEndDate = endDate;


        // 4. Account 엔티티 (용돈 계좌) 설정 - user 필드 사용하도록 수정
        allowanceAccount = Account.builder()
                .id(1L)
                .user(mockChildUser) // User 엔티티 참조로 변경
                .accountNo(ACCOUNT_NO)
                .type(AccountType.ALLOWANCE)
                .build();

        // 5. CoreTransactionItemRes Mock 데이터 설정
        // 레코드 구조: Long transactionId, String merchantName, BigDecimal amount, String code, 
        //             LocalDateTime transactionDate, Category category, BigDecimal balanceAfter, String transactionType
        LocalDateTime transactionTime = LocalDateTime.of(2023, 10, 25, 14, 30);
        List<CoreTransactionItemRes> transactionItems = List.of(
                new CoreTransactionItemRes(
                        1001L,
                        "용돈 입금",
                        new BigDecimal("50000"),
                        "DEPOSIT", // 또는 "D"
                        transactionTime,
                        Category.TRANSFER,
                        new BigDecimal("150000"),
                        "TRANSFER"
                ),
                new CoreTransactionItemRes(
                        1002L,
                        "편의점",
                        new BigDecimal("-5000"),
                        "WITHDRAW", // 또는 "W"
                        transactionTime.minusHours(1),
                        Category.FOOD,
                        new BigDecimal("145000"),
                        "LUMP_SUM"
                )
        );

        // 6. CoreTransactionHistoryRes Mock 데이터 설정 (balance 필드 추가 반영)
        // 레코드 구조: List<CoreTransactionItemRes> transactions, BigDecimal balance
        coreRes = new CoreTransactionHistoryRes(transactionItems, FINAL_BALANCE);
    }

    // ----------------------------------------------------------------------------------
    // 1. TC-HISTORY-001: 본인 용돈 계좌 거래 내역 조회 (CHILD)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-HISTORY-001: 자녀가 본인 용돈 계좌 거래 내역 조회 성공")
    void getHistory_ChildAccessingOwnAllowanceAccount_Success() {
        // GIVEN
        // 1. accountRepository.findByUserIdAndType() 호출 시 용돈 계좌 반환 Mocking
        when(accountRepository.findByUserIdAndType(CHILD_USER_ID, AccountType.ALLOWANCE))
                .thenReturn(Optional.of(allowanceAccount));

        // 2. coreAccountClient.getAccountTransactionsByPeriod() 호출 시 Core 응답 반환 Mocking
        when(coreAccountClient.getAccountTransactionsByPeriod(
                eq(ACCOUNT_NO), eq(coreClientStartDate), eq(coreClientEndDate)))
                .thenReturn(coreRes);

        // WHEN
        // AccountHistoryServiceImpl.getHistory()는 List<AccountHistoryRes>를 반환합니다.
        List<AccountHistoryRes> actualHistoryList = accountHistoryService.getHistory(
                CHILD_USER_ID, historyReq, childContext);

        // THEN
        // 1. 결과 리스트 검증
        assertNotNull(actualHistoryList);
        assertEquals(2, actualHistoryList.size());

        // 2. 첫 번째 항목 (입금) 검증 - CoreTransactionItemRes(1001L) -> AccountHistoryRes
        AccountHistoryRes depositItem = actualHistoryList.get(0);

        // AccountHistoryRes 레코드 구조: 
        // Long transactionId, String code, String merchant, String amount, String balanceAfter, String category, String timestamp
        assertEquals(1001L, depositItem.transactionId());
        assertEquals("DEPOSIT", depositItem.code());
        assertEquals("용돈 입금", depositItem.merchant()); // merchantName -> merchant
        assertEquals("50,000", depositItem.amount()); // 절대값, 포맷팅된 문자열
        assertEquals("150,000", depositItem.balanceAfter()); // 콤마 포맷팅된 잔액
        assertEquals(getKoreanName(Category.TRANSFER), depositItem.category()); // categoryName -> category
        assertEquals(LocalDateTime.of(2023, 10, 25, 14, 30).format(TIMESTAMP_FORMATTER), depositItem.timestamp()); // 포맷팅된 시간

        // 3. 두 번째 항목 (출금) 검증 - CoreTransactionItemRes(1002L) -> AccountHistoryRes
        AccountHistoryRes withdrawalItem = actualHistoryList.get(1);

        assertEquals(1002L, withdrawalItem.transactionId());
        assertEquals("WITHDRAW", withdrawalItem.code());
        assertEquals("편의점", withdrawalItem.merchant()); // merchantName -> merchant
        assertEquals("5,000", withdrawalItem.amount()); // 절대값, 포맷팅된 문자열
        assertEquals("145,000", withdrawalItem.balanceAfter()); // 콤마 포맷팅된 잔액
        assertEquals(getKoreanName(Category.FOOD), withdrawalItem.category()); // categoryName -> category
        assertEquals(LocalDateTime.of(2023, 10, 25, 13, 30).format(TIMESTAMP_FORMATTER), withdrawalItem.timestamp()); // 포맷팅된 시간


        // 4. 메서드 호출 검증
        verify(accountRepository, times(1))
                .findByUserIdAndType(eq(CHILD_USER_ID), eq(AccountType.ALLOWANCE));
        // DTO의 LocalDate 필드 값 (String 변환 값)으로 CoreAccountClient가 호출되었는지 검증
        verify(coreAccountClient, times(1))
                .getAccountTransactionsByPeriod(eq(ACCOUNT_NO), eq(coreClientStartDate), eq(coreClientEndDate));
    }

    // ----------------------------------------------------------------------------------
    // 2. 예외 처리 테스트: 계좌를 찾을 수 없는 경우
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-HISTORY-001_Exception: 용돈 계좌를 찾을 수 없는 경우 NOT_FOUND_ENTITY 발생")
    void getHistory_AccountNotFound_ThrowsException() {
        // GIVEN
        when(accountRepository.findByUserIdAndType(CHILD_USER_ID, AccountType.ALLOWANCE))
                .thenReturn(Optional.empty()); // 계좌 없음 Mocking

        // WHEN & THEN
        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            accountHistoryService.getHistory(CHILD_USER_ID, historyReq, childContext);
        });

        assertEquals(ErrorBaseCode.NOT_FOUND_ENTITY, thrown.getErrorCode());
    }

    // ----------------------------------------------------------------------------------
    // 3. 예외 처리 테스트: 다른 사람의 계좌를 조회 시도 (인가 실패)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-HISTORY-001_Exception: 자녀가 다른 자녀의 계좌 조회 시도 시 UNAUTHORIZED 발생")
    void getHistory_ChildAccessingOtherUserAccount_ThrowsException() {
        // GIVEN
        Long OTHER_CHILD_ID = 200L; // 다른 사용자의 ID

        // WHEN & THEN
        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            // childContext(ID: 100L)로 OTHER_CHILD_ID(200L)의 계좌 조회 시도
            accountHistoryService.getHistory(OTHER_CHILD_ID, historyReq, childContext);
        });

        assertEquals(ErrorBaseCode.UNAUTHORIZED, thrown.getErrorCode());
    }
}