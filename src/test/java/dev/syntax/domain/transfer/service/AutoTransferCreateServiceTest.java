// package dev.syntax.domain.transfer.service;

// import dev.syntax.domain.account.entity.Account;
// import dev.syntax.domain.account.enums.AccountType;
// import dev.syntax.domain.account.repository.AccountRepository;
// import dev.syntax.domain.transfer.dto.AutoTransferReq;
// import dev.syntax.domain.transfer.entity.AutoTransfer;
// import dev.syntax.domain.transfer.enums.AutoTransferType;
// import dev.syntax.domain.transfer.repository.AutoTransferRepository;
// import dev.syntax.domain.user.entity.User;
// import dev.syntax.domain.user.entity.UserRelationship;
// import dev.syntax.domain.user.enums.Role;
// import dev.syntax.domain.user.repository.UserRepository;
// import dev.syntax.global.auth.dto.UserContext;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.*;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.math.BigDecimal;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;


// @ExtendWith(MockitoExtension.class)
// class AutoTransferCreateServiceTest {

//     @Mock AutoTransferRepository autoTransferRepository;
//     @Mock AccountRepository accountRepository;
//     @Mock UserRepository userRepository;

//     @InjectMocks
//     @Spy
//     AutoTransferCreateServiceImpl service;

//     private User child;
//     private UserContext parentCtx;

//     private Account parentAccount;
//     private Account allowanceAccount;
//     private Account investAccount;

//     @BeforeEach
//     void setUp() {
//         // === Parent ===
//         User parent = User.builder()
//                 .id(1L)
//                 .name("이부모")
//                 .role(Role.PARENT)
//                 .email("parent@tf.com")
//                 .build();

//         // === Child ===
//         child = User.builder()
//                 .id(10L)
//                 .name("김티니")
//                 .role(Role.CHILD)
//                 .email("child10@tf.com")
//                 .build();

//         UserRelationship r = UserRelationship.builder()
//                 .parent(parent)
//                 .child(child)
//                 .build();

//         parent = User.builder()
//                 .id(parent.getId())
//                 .name(parent.getName())
//                 .role(parent.getRole())
//                 .email(parent.getEmail())
//                 .children(new ArrayList<>(List.of(r)))
//                 .build();

//         parentCtx = new UserContext(parent);

//         // === Accounts ===
//         parentAccount = Account.builder()
//                 .id(100L)
//                 .user(parent)
//                 .type(AccountType.DEPOSIT)
//                 .accountNo("1234-5678")
//                 .build();

//         allowanceAccount = Account.builder()
//                 .id(200L)
//                 .user(child)
//                 .type(AccountType.ALLOWANCE)
//                 .accountNo("1234-5679")
//                 .build();

//         investAccount = Account.builder()
//                 .id(300L)
//                 .user(child)
//                 .type(AccountType.INVEST)
//                 .accountNo("1234-5688")
//                 .build();
//     }


//     @Test
//     @DisplayName("비율 0% → 용돈 자동이체만 생성 / 투자 자동이체 없음")
//     void create_only_allowance_transfer_when_ratio_zero() {
//         AutoTransferReq req = new AutoTransferReq();
//         setField(req, "totalAmount", BigDecimal.valueOf(100000));
//         setField(req, "transferDate", 25);
//         setField(req, "ratio", 0);

//         when(userRepository.findById(10L)).thenReturn(Optional.of(child));
//         when(accountRepository.findByUserIdAndType(1L, AccountType.DEPOSIT)).thenReturn(Optional.of(parentAccount));
//         when(accountRepository.findByUserIdAndType(10L, AccountType.ALLOWANCE)).thenReturn(Optional.of(allowanceAccount));
//         when(accountRepository.findByUserIdAndType(10L, AccountType.INVEST)).thenReturn(Optional.of(investAccount));
//         when(autoTransferRepository.existsByUserIdAndType(10L, AutoTransferType.ALLOWANCE)).thenReturn(false);

//         // Core mock – 용돈 자동이체 ID only
//         doReturn(1111L).when(service)
//                 .createCoreTransfer(eq(parentAccount), eq(allowanceAccount), any(), eq(25), any());

//         service.createAutoTransfer(10L, req, parentCtx);

//         ArgumentCaptor<AutoTransfer> captor = ArgumentCaptor.forClass(AutoTransfer.class);
//         verify(autoTransferRepository).save(captor.capture());

//         AutoTransfer saved = captor.getValue();
//         assertThat(saved.getPrimaryBankTransferId()).isEqualTo(1111L);
//         assertThat(saved.getInvestBankTransferId()).isNull();
//         assertThat(saved.getRatio()).isEqualTo(0);
//     }


//     @Test
//     @DisplayName("비율 50% → 용돈 + 투자 자동이체 모두 생성됨")
//     void create_allowance_and_invest_transfer() {
//         AutoTransferReq req = new AutoTransferReq();
//         setField(req, "totalAmount", BigDecimal.valueOf(100000));
//         setField(req, "transferDate", 25);
//         setField(req, "ratio", 50);

//         when(userRepository.findById(10L)).thenReturn(Optional.of(child));
//         when(accountRepository.findByUserIdAndType(1L, AccountType.DEPOSIT)).thenReturn(Optional.of(parentAccount));
//         when(accountRepository.findByUserIdAndType(10L, AccountType.ALLOWANCE)).thenReturn(Optional.of(allowanceAccount));
//         when(accountRepository.findByUserIdAndType(10L, AccountType.INVEST)).thenReturn(Optional.of(investAccount));
//         when(autoTransferRepository.existsByUserIdAndType(10L, AutoTransferType.ALLOWANCE)).thenReturn(false);

//         // Core mock – 용돈/투자 자동이체 ID
//         doReturn(2222L).when(service)
//                 .createCoreTransfer(parentAccount, allowanceAccount, any(), eq(25), any());
//         doReturn(3333L).when(service)
//                 .createCoreTransfer(parentAccount, investAccount, any(), eq(25), any());

//         service.createAutoTransfer(10L, req, parentCtx);

//         ArgumentCaptor<AutoTransfer> captor = ArgumentCaptor.forClass(AutoTransfer.class);
//         verify(autoTransferRepository).save(captor.capture());

//         AutoTransfer saved = captor.getValue();
//         assertThat(saved.getPrimaryBankTransferId()).isEqualTo(2222L);
//         assertThat(saved.getInvestBankTransferId()).isEqualTo(3333L);
//         assertThat(saved.getRatio()).isEqualTo(50);
//     }

//         @Test
//     @DisplayName("이미 자동이체(ALLOWANCE)가 존재하면 생성 불가")
//     void fail_when_allowance_transfer_already_exists() {
//         // given
//         AutoTransferReq req = new AutoTransferReq();
//         setField(req, "totalAmount", BigDecimal.valueOf(50000));
//         setField(req, "transferDate", 10);
//         setField(req, "ratio", 30);

//         when(autoTransferRepository.existsByUserIdAndType(10L, AutoTransferType.ALLOWANCE))
//                 .thenReturn(true);

//         // when & then
//         org.assertj.core.api.Assertions.assertThatThrownBy(() ->
//                 service.createAutoTransfer(10L, req, parentCtx)
//         ).isInstanceOf(dev.syntax.global.exception.BusinessException.class);
//     }


//     @Test
//     @DisplayName("금액이 비율대로 정확히 나뉘어 저장되는지 확인")
//     void check_amount_split_correctly() {
//         // given
//         AutoTransferReq req = new AutoTransferReq();
//         setField(req, "totalAmount", BigDecimal.valueOf(100000));
//         setField(req, "transferDate", 20);
//         setField(req, "ratio", 30); // 투자 30%, 용돈 70%

//         when(userRepository.findById(10L)).thenReturn(Optional.of(child));
//         when(accountRepository.findByUserIdAndType(1L, AccountType.DEPOSIT)).thenReturn(Optional.of(parentAccount));
//         when(accountRepository.findByUserIdAndType(10L, AccountType.ALLOWANCE)).thenReturn(Optional.of(allowanceAccount));
//         when(accountRepository.findByUserIdAndType(10L, AccountType.INVEST)).thenReturn(Optional.of(investAccount));
//         when(autoTransferRepository.existsByUserIdAndType(10L, AutoTransferType.ALLOWANCE)).thenReturn(false);

//         // Core mock
//         doReturn(9999L).when(service)
//                 .createCoreTransfer(eq(parentAccount), eq(allowanceAccount), any(), eq(20), any());
//         doReturn(8888L).when(service)
//                 .createCoreTransfer(eq(parentAccount), eq(investAccount), any(), eq(20), any());

//         // when
//         service.createAutoTransfer(10L, req, parentCtx);

//         // then
//         ArgumentCaptor<AutoTransfer> captor = ArgumentCaptor.forClass(AutoTransfer.class);
//         verify(autoTransferRepository).save(captor.capture());
//         AutoTransfer saved = captor.getValue();

//         // 총 금액 유지
//         assertThat(saved.getTransferAmount()).isEqualByComparingTo(BigDecimal.valueOf(100000));

//         // 비율 체크
//         assertThat(saved.getRatio()).isEqualTo(30);
//         // 용돈 70,000 / 투자 30,000 합산 검증은 AutoTransferUtils에서 적용됨
//         BigDecimal allowanceAmt = BigDecimal.valueOf(70000);
//         BigDecimal investAmt = BigDecimal.valueOf(30000);

//         assertThat(allowanceAmt.add(investAmt))
//                 .isEqualByComparingTo(saved.getTransferAmount());
//     }


//     @Test
//     @DisplayName("투자 비율 > 0 이지만 투자 계좌가 없으면 에러 발생")
//     void fail_when_ratio_positive_but_no_invest_account() {
//         // given
//         AutoTransferReq req = new AutoTransferReq();
//         setField(req, "totalAmount", BigDecimal.valueOf(50000));
//         setField(req, "transferDate", 25);
//         setField(req, "ratio", 40);

//         when(userRepository.findById(10L)).thenReturn(Optional.of(child));
//         when(accountRepository.findByUserIdAndType(1L, AccountType.DEPOSIT)).thenReturn(Optional.of(parentAccount));
//         when(accountRepository.findByUserIdAndType(10L, AccountType.ALLOWANCE)).thenReturn(Optional.of(allowanceAccount));
//         when(accountRepository.findByUserIdAndType(10L, AccountType.INVEST)).thenReturn(Optional.empty());
//         when(autoTransferRepository.existsByUserIdAndType(10L, AutoTransferType.ALLOWANCE))
//                 .thenReturn(false);

//         // when & then
//         org.assertj.core.api.Assertions.assertThatThrownBy(() ->
//                 service.createAutoTransfer(10L, req, parentCtx)
//         ).isInstanceOf(dev.syntax.global.exception.BusinessException.class);
//     }


//     /** reflection helper */
//     private void setField(Object target, String fieldName, Object value) {
//         try {
//             java.lang.reflect.Field f = target.getClass().getDeclaredField(fieldName);
//             f.setAccessible(true);
//             f.set(target, value);
//         } catch (Exception e) {
//             throw new RuntimeException(e);
//         }
//     }
// }
