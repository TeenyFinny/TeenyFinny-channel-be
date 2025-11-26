package dev.syntax.domain.transfer.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transfer.client.CoreAutoTransferClient;
import dev.syntax.domain.transfer.dto.AutoTransferReq;
import dev.syntax.domain.transfer.dto.CoreAutoTransferReq;
import dev.syntax.domain.transfer.dto.CoreAutoTransferRes;
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
    private final CoreAutoTransferClient coreAutoTransferClient;

    /**
     * 자동이체 설정을 생성합니다.
     * <p>
     * 1. 부모 권한 검증 (자녀 목록 확인)
     * 2. 중복 설정 확인 (이미 존재하는 경우 예외 발생)
     * 3. 사용자 및 계좌 조회 (부모 입출금, 자녀 용돈/투자/목표)
     * 4. 입력값 검증 (비율 범위, 투자 계좌 필수 여부 등)
     * 5. 금액 계산 (ALLOWANCE: 비율 분배, GOAL: 전체 금액)
     * 6. 자동이체 정보 저장 (Mock ID 생성 포함)
     * </p>
     *
     * @param childId 자녀 ID
     * @param req     자동이체 설정 요청 정보
     * @param ctx     사용자 컨텍스트
     * @throws BusinessException 권한 없음, 계좌 없음, 중복 설정, 잘못된 입력값 등
     */
    @Override
    public void createAutoTransfer(Long childId, AutoTransferReq req, UserContext ctx) {

        validateParentAccess(ctx, childId);
        // 중복 설정 여부 확인
        if (autoTransferRepository.existsByUserIdAndType(childId, req.getType())) {
            throw new BusinessException(ErrorBaseCode.AUTO_TRANSFER_ALREADY_EXISTS);
        }
        // 부모 계좌 조회
        Account parentAccount = accountRepository.findByUserIdAndType(ctx.getId(), AccountType.DEPOSIT)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        // 자녀 정보 조회
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        // 용돈 계좌 조회
        Account allowanceAccount = accountRepository.findByUserIdAndType(childId, AccountType.ALLOWANCE)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        // 투자 계좌 조회 (optional)
        Account investAccount = accountRepository.findByUserIdAndType(childId, AccountType.INVEST)
                .orElse(null);

        // 비율 검증
        if (req.getRatio() < 0 || req.getRatio() > 100) {
            throw new BusinessException(ErrorBaseCode.INVALID_RATIO_VALUE);
        }

        // 투자 비율 > 0인데 계좌 없음
        if (req.getRatio() > 0 && investAccount == null) {
            throw new BusinessException(ErrorBaseCode.INVEST_ACCOUNT_REQUIRED);
        }

        // ratio에 따른 용돈/투자 금액 계산
        BigDecimal[] amounts = AutoTransferUtils.calculateAmounts(req.getTotalAmount(), req.getRatio());
        BigDecimal allowanceAmount = amounts[0];
        BigDecimal investAmount = amounts[1];

        CoreAutoTransferReq allowanceAutoTransferReq = new CoreAutoTransferReq(
                childId,
                parentAccount.getId(),
                allowanceAccount.getId(),
                allowanceAmount,
                req.getTransferDate(),
                "용돈");

        CoreAutoTransferRes coreAllowanceRes = coreAutoTransferClient.createAutoTransfer(childId,
                allowanceAutoTransferReq);
        if (coreAllowanceRes == null || coreAllowanceRes.autoTransferId() == null) {
            throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
        }
        if (req.getRatio() > 0) {
            CoreAutoTransferReq investCoreAutoTransferReq = new CoreAutoTransferReq(
                    childId,
                    parentAccount.getId(),
                    investAccount.getId(),
                    investAmount,
                    req.getTransferDate(),
                    "용돈");
            CoreAutoTransferRes coreRes = coreAutoTransferClient.createAutoTransfer(childId, investCoreAutoTransferReq);
            if (coreRes == null || coreRes.autoTransferId() == null) {
                throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
            }
        }

    }

    private void validateParentAccess(UserContext ctx, Long childId) {

        if (!Role.PARENT.name().equals(ctx.getRole())) {
            throw new BusinessException(ErrorBaseCode.PARENT_ONLY_FEATURE);
        }

        if (!ctx.getChildren().contains(childId)) {
            throw new BusinessException(ErrorBaseCode.INVALID_CHILD);
        }
    }

}
