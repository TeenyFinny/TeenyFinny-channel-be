package dev.syntax.domain.transfer.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transfer.client.CoreAutoTransferClient;
import dev.syntax.domain.transfer.dto.AutoTransferReq; // <--- 새로 추가
import dev.syntax.domain.transfer.dto.CoreCreateAutoTransferReq;
import dev.syntax.domain.transfer.dto.CoreCreateAutoTransferRes;
import dev.syntax.domain.transfer.entity.AutoTransfer;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.transfer.repository.AutoTransferRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoSettings; // 추가
import org.mockito.quality.Strictness; // 추가

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import java.util.Arrays; // 추가
import java.util.HashSet; // 추가
import java.util.List; // 추가

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 불필요한 Stubbing 예외 방지
class AutoTransferServiceImplTest {

    @InjectMocks
    private AutoTransferServiceImpl autoTransferService;

    @Mock
    private AutoTransferRepository autoTransferRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CoreAutoTransferClient coreAutoTransferClient;

    // Mock Data
    private final Long PARENT_ID = 1L;
    private final Long CHILD_ID = 10L;
	private final Long CORE_CHILD_ID = 11L;
    private final Long PARENT_DEPOSIT_ACCOUNT_ID = 100L;
    private final Long CHILD_ALLOWANCE_ACCOUNT_ID = 200L;
    private final Long CORE_ALLOWANCE_TRANSFER_ID = 900L;
    private final BigDecimal TOTAL_AMOUNT = new BigDecimal("100000");
    private final Integer TRANSFER_DATE = 25;
    private final Integer RATIO_ALLOWANCE_ONLY = 0; // 용돈만 설정

    private UserContext parentContext;
    private User mockParent;
    private User mockChild;
    private Account parentDepositAccount;
    private Account childAllowanceAccount;
    private AutoTransferReq allowanceReq;

    // Helper: Mock DTOs/Entities
    // NOTE: 실제 프로젝트의 DTO/Entity 구조를 따르도록 가정하고 단순화합니다.

    // 1. AutoTransferReq DTO (request) - *상단 import를 사용하므로 내부 정의를 제거함*
    // public record AutoTransferReq(
    //         AutoTransferType type,
    //         BigDecimal totalAmount,
    //         Integer transferDate,
    //         Integer ratio
    // ) {}

    // 2. CoreCreateAutoTransferRes DTO (Core Response) - *상단 import를 사용하므로 내부 정의를 제거함*
    // public record CoreCreateAutoTransferRes(
    //     Long autoTransferId
    // ) {}

    // 3. AutoTransfer Entity Mock (for save verification)
    private AutoTransfer createMockAutoTransfer(
            Long id, User user, Account account, BigDecimal amount, Integer date, Integer ratio, AutoTransferType type, Long primaryId, Long investId) {
        // 실제 엔티티는 Lombok @Builder와 updateAutoTransfer 메서드를 가지고 있다고 가정하고,
        // 테스트를 위해 필요한 Getter/Setter만 포함한 Mock 객체 구조를 반환합니다.
        AutoTransfer mockTransfer = mock(AutoTransfer.class);
        when(mockTransfer.getId()).thenReturn(id);
        when(mockTransfer.getUser()).thenReturn(user);
        when(mockTransfer.getAccount()).thenReturn(account);
        when(mockTransfer.getTransferAmount()).thenReturn(amount);
        when(mockTransfer.getTransferDate()).thenReturn(date);
        when(mockTransfer.getRatio()).thenReturn(ratio);
        when(mockTransfer.getType()).thenReturn(type);
        when(mockTransfer.getPrimaryBankTransferId()).thenReturn(primaryId);
        when(mockTransfer.getInvestBankTransferId()).thenReturn(investId);
        return mockTransfer;
    }


    @BeforeEach
    void setUp() {
        // 1. Mock User 및 Context 설정
        // Lombok Builder 대신 Mockito로 User 객체를 완전히 Mocking하여 Role 반환을 보장합니다.
        mockChild = mock(User.class);
        when(mockChild.getId()).thenReturn(CHILD_ID);
        when(mockChild.getRole()).thenReturn(Role.CHILD);
		when(mockChild.getCoreUserId()).thenReturn(CORE_CHILD_ID);
        mockParent = mock(User.class);
        when(mockParent.getId()).thenReturn(PARENT_ID);
        when(mockParent.getRole()).thenReturn(Role.PARENT);

        // UserContext가 builder() 메서드를 제공하지 않아 발생하는 오류를 해결하기 위해
        // UserContext 객체를 Mocking하고 필요한 Getter만 정의합니다.
        parentContext = mock(UserContext.class);
        // UserContext의 핵심 필드 Mocking 추가 (validateParentAccess에서 사용될 수 있음)
        when(parentContext.getId()).thenReturn(PARENT_ID);
        when(parentContext.getRole()).thenReturn(Role.PARENT.name());

        when(parentContext.getUser()).thenReturn(mockParent);
        // UserContext의 getChildren()이 List<Long>을 반환하도록 정의되어 있으므로 이에 맞게 수정합니다.
        when(parentContext.getChildren()).thenReturn(Arrays.asList(CHILD_ID));

        // 2. Mock Account 설정 (Account 엔티티 구조 변경 반영)
        // Account.builder()는 실제 엔티티 구현에 의존하므로, Role을 가진 mockParent를 사용합니다.
        parentDepositAccount = Account.builder()
                .id(PARENT_DEPOSIT_ACCOUNT_ID)
                .user(mockParent)
                .type(AccountType.DEPOSIT)
                .accountNo("P1234")
                .build();
        childAllowanceAccount = Account.builder()
                .id(CHILD_ALLOWANCE_ACCOUNT_ID)
                .user(mockChild)
                .type(AccountType.ALLOWANCE)
                .accountNo("C5678")
                .build();

        // 3. AutoTransferReq 설정 (용돈만, 비율 0%)
        // AutoTransferReq DTO가 생성자가 없거나 빌더를 사용하지 않아 발생하는 오류를 해결하기 위해
        // DTO 객체를 Mocking하고 필요한 Getter만 정의합니다.
        allowanceReq = mock(AutoTransferReq.class);
        when(allowanceReq.getType()).thenReturn(AutoTransferType.ALLOWANCE);
        when(allowanceReq.getTotalAmount()).thenReturn(TOTAL_AMOUNT);
        when(allowanceReq.getTransferDate()).thenReturn(TRANSFER_DATE);
        when(allowanceReq.getRatio()).thenReturn(RATIO_ALLOWANCE_ONLY);
    }

    // ----------------------------------------------------------------------------------
    // TC-TRANSFER-003: 부모가 용돈 자동이체 설정 (Allowance Only)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-TRANSFER-003: 부모가 용돈 계좌에 대해 자동이체 (비율 0%) 설정을 성공적으로 생성")
    void createAutoTransfer_AllowanceOnly_Success() {
        // GIVEN
        // 1. 중복 설정 없음
        when(autoTransferRepository.existsByUserIdAndType(CHILD_ID, AutoTransferType.ALLOWANCE)).thenReturn(false);

        // 2. 필요한 엔티티 조회 성공
        // mockParent의 Role이 PARENT임을 setUp에서 보장했으므로, 이 테스트는 통과해야 합니다.
        when(accountRepository.findByUserIdAndType(PARENT_ID, AccountType.DEPOSIT))
                .thenReturn(Optional.of(parentDepositAccount));
        when(accountRepository.findByUserIdAndType(CHILD_ID, AccountType.ALLOWANCE))
                .thenReturn(Optional.of(childAllowanceAccount));
        when(userRepository.findById(CHILD_ID))
                .thenReturn(Optional.of(mockChild));
        // 투자 계좌는 없음 (ratio=0이므로 있어도 무방, 없어도 무방)
        when(accountRepository.findByUserIdAndType(CHILD_ID, AccountType.INVEST))
                .thenReturn(Optional.empty());


        // 3. Core Bank API Mocking (용돈 자동이체 생성 성공)
        CoreCreateAutoTransferRes coreAllowanceRes = new CoreCreateAutoTransferRes(CORE_ALLOWANCE_TRANSFER_ID);
        // 투자 비율이 0이므로, 용돈 자동이체만 1번 호출됨
        when(coreAutoTransferClient.createAutoTransfer(any(CoreCreateAutoTransferReq.class)))
                .thenReturn(coreAllowanceRes);

        // 4. Repository save Mocking (저장 시 Mock 객체 반환)
        AutoTransfer savedTransfer = createMockAutoTransfer(
                1L, mockChild, childAllowanceAccount, TOTAL_AMOUNT, TRANSFER_DATE, RATIO_ALLOWANCE_ONLY,
                AutoTransferType.ALLOWANCE, CORE_ALLOWANCE_TRANSFER_ID, null);
        when(autoTransferRepository.save(any(AutoTransfer.class))).thenReturn(savedTransfer);

        // WHEN
        autoTransferService.createAutoTransfer(CHILD_ID, allowanceReq, parentContext);

        // THEN
        // 1. 중복 확인 검증
        verify(autoTransferRepository, times(1)).existsByUserIdAndType(CHILD_ID, AutoTransferType.ALLOWANCE);

        // 2. Core Bank API 호출 검증 (용돈 자동이체만 1번 호출)
        ArgumentCaptor<CoreCreateAutoTransferReq> coreReqCaptor = ArgumentCaptor.forClass(CoreCreateAutoTransferReq.class);
        verify(coreAutoTransferClient, times(1)).createAutoTransfer(coreReqCaptor.capture());

        CoreCreateAutoTransferReq capturedReq = coreReqCaptor.getValue();
        // DTO 필드명 변경 반영
        assertEquals(CORE_CHILD_ID, capturedReq.userId());
        assertEquals(PARENT_DEPOSIT_ACCOUNT_ID, capturedReq.fromAccountId());
        assertEquals(CHILD_ALLOWANCE_ACCOUNT_ID, capturedReq.toAccountId());
        assertEquals(TOTAL_AMOUNT, capturedReq.amount()); // Ratio=0이므로 용돈 금액은 전체 금액과 동일
        assertEquals(TRANSFER_DATE, capturedReq.transferDay()); // 필드명 변경 반영
        assertEquals("용돈", capturedReq.memo()); // memo 필드 추가 검증

        // 3. 투자 계좌 조회는 실행되지만, Core Invest 생성 호출은 없어야 함
        verify(accountRepository, times(1)).findByUserIdAndType(CHILD_ID, AccountType.INVEST);
        verify(coreAutoTransferClient, never()).deleteAutoTransfer(anyLong()); // 롤백도 없어야 함

        // 4. Channel DB 저장 검증
        ArgumentCaptor<AutoTransfer> autoTransferCaptor = ArgumentCaptor.forClass(AutoTransfer.class);
        verify(autoTransferRepository, times(1)).save(autoTransferCaptor.capture());

        AutoTransfer capturedTransfer = autoTransferCaptor.getValue();
        assertEquals(TOTAL_AMOUNT, capturedTransfer.getTransferAmount());
        assertEquals(RATIO_ALLOWANCE_ONLY, capturedTransfer.getRatio());
        assertEquals(CORE_ALLOWANCE_TRANSFER_ID, capturedTransfer.getPrimaryBankTransferId());
        assertNull(capturedTransfer.getInvestBankTransferId()); // 투자 ID는 null
    }

    // ----------------------------------------------------------------------------------
    // 기타 예외 테스트 (옵션)
    // ----------------------------------------------------------------------------------

    @Test
    @DisplayName("TC-TRANSFER-004: 부모가 아닌 사용자가 자동이체 생성 시도 시 PARENT_ONLY_FEATURE 발생")
    void createAutoTransfer_NotParent_ThrowsException() {
        // GIVEN
        // setUp에서 mockChild를 이미 CHILD 역할로 설정했습니다. 이를 사용합니다.

        // UserContext를 Mock하여 PARENT가 아닌 사용자 Context를 정의합니다.
        UserContext childContext = mock(UserContext.class);
        when(childContext.getUser()).thenReturn(mockChild);

        // WHEN & THEN
        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            autoTransferService.createAutoTransfer(CHILD_ID, allowanceReq, childContext);
        });

        assertEquals(ErrorBaseCode.PARENT_ONLY_FEATURE, thrown.getErrorCode());
    }
}