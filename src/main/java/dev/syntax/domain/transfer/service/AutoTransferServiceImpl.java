package dev.syntax.domain.transfer.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transfer.client.CoreAutoTransferClient;
import dev.syntax.domain.transfer.dto.AutoTransferReq;
import dev.syntax.domain.transfer.dto.AutoTransferRes;
import dev.syntax.domain.transfer.dto.CoreAutoTransferReq;
import dev.syntax.domain.transfer.dto.CoreAutoTransferRes;
import dev.syntax.domain.transfer.entity.AutoTransfer;
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
public class AutoTransferServiceImpl implements AutoTransferService {

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

        Account parentAccount = getAccount(ctx.getId(), AccountType.DEPOSIT);
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));
        Account allowanceAccount = getAccount(childId, AccountType.ALLOWANCE);
        Account investAccount = accountRepository.findByUserIdAndType(childId, AccountType.INVEST).orElse(null);

        validateRatio(req.getRatio(), investAccount);

        BigDecimal[] amounts = AutoTransferUtils.calculateAmounts(req.getTotalAmount(), req.getRatio());
        BigDecimal allowanceAmount = amounts[0];
        BigDecimal investAmount = amounts[1];

        CoreAutoTransferReq allowanceReq = createCoreReq(childId, parentAccount.getId(), allowanceAccount.getId(),
                allowanceAmount, req.getTransferDate());
        CoreAutoTransferRes coreAllowanceRes = coreAutoTransferClient.createAutoTransfer(allowanceReq);

        if (coreAllowanceRes == null || coreAllowanceRes.autoTransferId() == null) {
            throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
        }

        Long investTransferId = null;
        if (req.getRatio() > 0) {
            CoreAutoTransferReq investReq = createCoreReq(childId, parentAccount.getId(), investAccount.getId(),
                    investAmount, req.getTransferDate());
            CoreAutoTransferRes coreInvestRes = coreAutoTransferClient.createAutoTransfer(investReq);

            if (coreInvestRes == null || coreInvestRes.autoTransferId() == null) {
                throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
            }
            investTransferId = coreInvestRes.autoTransferId();
        }

        saveAutoTransfer(child, allowanceAccount, req, coreAllowanceRes.autoTransferId(), investTransferId);
    }

    @Override
    public AutoTransferRes updateAutoTransfer(Long childId, AutoTransferReq req, UserContext ctx) {
        validateParentAccess(ctx, childId);

        AutoTransfer existingTransfer = autoTransferRepository.findByUserIdAndType(childId, req.getType())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.AUTO_TRANSFER_NOT_FOUND));

        Account parentAccount = getAccount(ctx.getId(), AccountType.DEPOSIT);
        Account allowanceAccount = getAccount(childId, AccountType.ALLOWANCE);
        Account investAccount = accountRepository.findByUserIdAndType(childId, AccountType.INVEST).orElse(null);

        validateRatio(req.getRatio(), investAccount);

        BigDecimal[] amounts = AutoTransferUtils.calculateAmounts(req.getTotalAmount(), req.getRatio());
        BigDecimal allowanceAmount = amounts[0];
        BigDecimal investAmount = amounts[1];

        // 1. Update Allowance
        CoreAutoTransferReq allowanceReq = createCoreReq(childId, parentAccount.getId(), allowanceAccount.getId(),
                allowanceAmount, req.getTransferDate());
        // 여기서 코어는 200 OK만 반환, Body 없음 → 예외 없으면 성공
        coreAutoTransferClient.updateAutoTransfer(existingTransfer.getPrimaryBankTransferId(), allowanceReq);

        // 2. Update Invest
        Long investTransferId = existingTransfer.getInvestBankTransferId();
        Long newInvestTransferId = investTransferId; // 기본값: 기존 ID 유지

        if (investTransferId != null) {
            // 예전에 투자 자동이체가 있었던 경우
            if (req.getRatio() > 0) {
                // 투자 비율 여전히 > 0 → 코어에서 update
                CoreAutoTransferReq investReq = createCoreReq(
                        childId,
                        parentAccount.getId(),
                        investAccount.getId(),
                        investAmount,
                        req.getTransferDate());
                coreAutoTransferClient.updateAutoTransfer(investTransferId, investReq);
            } else {
                // 이제 투자 비율 0 → 더 이상 투자 자동이체 필요 없음
                newInvestTransferId = null;
                // 필요하다면: 코어에 "해지" API 호출도 가능 (지금 설계엔 없으니 채널에서만 null 처리)
            }
        } else {
            // 예전에는 투자 자동이체가 없었음
            if (req.getRatio() > 0) {
                // 새롭게 투자 자동이체 생성 필요
                CoreAutoTransferReq investReq = createCoreReq(
                        childId,
                        parentAccount.getId(),
                        investAccount.getId(),
                        investAmount,
                        req.getTransferDate());
                CoreAutoTransferRes res = coreAutoTransferClient.createAutoTransfer(investReq);
                if (res == null || res.autoTransferId() == null) {
                    throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
                }
                newInvestTransferId = res.autoTransferId();
            }
        }

        // 7) 채널 DB 엔티티 수정
        existingTransfer.updateAutoTransfer(req, newInvestTransferId);
        autoTransferRepository.save(existingTransfer);

        // 8) 응답 리턴
        return AutoTransferRes.of(
                existingTransfer.getId(),
                existingTransfer.getTransferAmount(),
                existingTransfer.getTransferDate(),
                existingTransfer.getRatio());
    }

    private void validateParentAccess(UserContext ctx, Long childId) {
        if (!Role.PARENT.name().equals(ctx.getRole())) {
            throw new BusinessException(ErrorBaseCode.PARENT_ONLY_FEATURE);
        }
        if (!ctx.getChildren().contains(childId)) {
            throw new BusinessException(ErrorBaseCode.INVALID_CHILD);
        }
    }

    private Account getAccount(Long userId, AccountType type) {
        return accountRepository.findByUserIdAndType(userId, type)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));
    }

    private void validateRatio(Integer ratio, Account investAccount) {
        if (ratio < 0 || ratio > 100) {
            throw new BusinessException(ErrorBaseCode.INVALID_RATIO_VALUE);
        }
        if (ratio > 0 && investAccount == null) {
            throw new BusinessException(ErrorBaseCode.INVEST_ACCOUNT_REQUIRED);
        }
    }

    private CoreAutoTransferReq createCoreReq(Long childId, Long fromId, Long toId, BigDecimal amount, Integer date) {
        return new CoreAutoTransferReq(childId, fromId, toId, amount, date, "용돈");
    }

    private AutoTransfer saveAutoTransfer(User user, Account account, AutoTransferReq req, Long primaryId,
            Long investId) {
        AutoTransfer transfer = AutoTransfer.builder()
                .user(user)
                .account(account)
                .transferAmount(req.getTotalAmount())
                .transferDate(req.getTransferDate())
                .ratio(req.getRatio())
                .type(req.getType())
                .primaryBankTransferId(primaryId)
                .investBankTransferId(investId)
                .build();
        return autoTransferRepository.save(transfer);
    }
}
