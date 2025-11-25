package dev.syntax.domain.transfer.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;

import dev.syntax.domain.transfer.dto.AutoTransferReq;
import dev.syntax.domain.transfer.dto.AutoTransferRes;
import dev.syntax.domain.transfer.entity.AutoTransfer;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.transfer.repository.AutoTransferRepository;
import dev.syntax.domain.transfer.utils.AutoTransferUtils;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 자동이체 서비스 구현체.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AutoTransferCreateServiceImpl implements AutoTransferCreateService {

    private final AutoTransferRepository autoTransferRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /**
     * 자동이체 설정을 생성합니다.
     * <p>
     * 1. 부모 권한 검증 (자녀 목록 확인)
     * 2. 중복 설정 확인 (이미 존재하는 경우 예외 발생)
     * 3. 사용자 및 계좌 조회 (부모 입출금, 자녀 용돈/투자)
     * 4. 입력값 검증 (비율 범위, 투자 계좌 필수 여부)
     * 5. 금액 계산 (총 금액을 비율에 따라 용돈/투자로 분배)
     * 6. 자동이체 정보 저장 (Mock ID 생성 포함)
     * </p>
     *
     * @param childId 자녀 ID
     * @param req 자동이체 설정 요청 정보
     * @param ctx 사용자 컨텍스트
     * @throws BusinessException 권한 없음, 계좌 없음, 중복 설정, 잘못된 입력값 등
     */
    @Override
    public void createAutoTransfer(Long childId, AutoTransferReq req, UserContext ctx) {

        validateParentAccess(ctx, childId);

        // 이미 자동이체 설정이 있다면 생성 불가
        if (autoTransferRepository.existsByUserIdAndType(childId, AutoTransferType.ALLOWANCE)) {
            throw new BusinessException(ErrorBaseCode.AUTO_TRANSFER_ALREADY_EXISTS);
        }
        // 부모의 입출금 계좌 조회
        Account parentAccount = accountRepository.findByUserIdAndType(ctx.getId(), AccountType.DEPOSIT)
                        .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        // 자녀 용돈 계좌 조회
        Account allowanceAccount = accountRepository.findByUserIdAndType(childId, AccountType.ALLOWANCE)
                        .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        // 자녀 투자 계좌 존재 여부 확인
        Account investAccount = accountRepository.findByUserIdAndType(childId, AccountType.INVEST)
                        .orElse(null);

        // 비율 검증 (0~100)
        if (req.getRatio() < 0 || req.getRatio() > 100) {
                throw new BusinessException(ErrorBaseCode.INVALID_RATIO_VALUE);
        }

        // 투자 비율 > 0인데 투자 계좌가 없다면 예외
        if (req.getRatio() > 0 && investAccount == null) {
                throw new BusinessException(ErrorBaseCode.INVEST_ACCOUNT_REQUIRED);
        }

        // 자동이체 금액 계산
        BigDecimal[] amounts = AutoTransferUtils.calculateAmounts(req.getTotalAmount(), req.getRatio());
        BigDecimal allowanceAmount = amounts[0];
        BigDecimal investAmount = amounts[1];

        Long primaryCoreId = createCoreTransfer(
                parentAccount,
                allowanceAccount,
                allowanceAmount,
                req.getTransferDate(),
                "용돈"
        );

        Long investCoreId = null;

        if (req.getRatio() > 0) {
            investCoreId = createCoreTransfer(
                    parentAccount,
                    investAccount,
                    investAmount,
                    req.getTransferDate(),
                    "용돈"
            );
        }

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        AutoTransfer autoTransfer = AutoTransfer.builder()
                .user(child)
                .account(allowanceAccount) // 용돈 계좌가 주계좌이기 때문에 용돈 계좌 저장
                .transferAmount(req.getTotalAmount())
                .transferDate(req.getTransferDate())
                .ratio(req.getRatio())
                .type(AutoTransferType.ALLOWANCE)
                .primaryBankTransferId(primaryCoreId)
                .investBankTransferId(investCoreId)     // 투자 비율이 0이면 null
                .build();

        autoTransferRepository.save(autoTransfer);
    }   

    private void validateParentAccess(UserContext ctx, Long childId) {

        if (!Role.PARENT.name().equals(ctx.getRole())) {
                throw new BusinessException(ErrorBaseCode.PARENT_ONLY_FEATURE);
        }

        if (!ctx.getChildren().contains(childId)) {
                throw new BusinessException(ErrorBaseCode.INVALID_CHILD);
        }
    }
    /**
     * 코어 자동이체 생성 (Mock)
     * 실제 연동 시 CoreBankClient.createAutoTransfer() 호출 예정
     */
    protected Long createCoreTransfer(Account from, Account to, BigDecimal amt, Integer date, String memo) {

        log.info("[CORE] 자동이체 생성 | FROM={} | TO={} | AMT={} | DATE={} | MEMO={}",
                from.getAccountNo(), to.getAccountNo(), amt, date, memo);

        // 실제 코어 연동 시 생성된 ID 반환
        return (long) (Math.random() * 1_000_000_000L);
    }
}
