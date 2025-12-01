package dev.syntax.domain.transfer.service;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transfer.client.CoreAutoTransferClient;
import dev.syntax.domain.transfer.dto.AutoTransferReq;
import dev.syntax.domain.transfer.dto.AutoTransferRes;
import dev.syntax.domain.transfer.dto.CoreAllowanceUpdateAutoTransferReq;
import dev.syntax.domain.transfer.dto.CoreCreateAutoTransferReq;
import dev.syntax.domain.transfer.dto.CoreCreateAutoTransferRes;
import dev.syntax.domain.transfer.entity.AutoTransfer;
import dev.syntax.domain.transfer.repository.AutoTransferRepository;
import dev.syntax.domain.transfer.utils.AutoTransferUtils;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
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

        CoreCreateAutoTransferReq allowanceReq = createCoreReq(childId, parentAccount.getId(), allowanceAccount.getId(),
                allowanceAmount, req.getTransferDate());
        CoreCreateAutoTransferRes coreAllowanceRes = coreAutoTransferClient.createAutoTransfer(allowanceReq);

        if (coreAllowanceRes == null || coreAllowanceRes.autoTransferId() == null) {
            throw new BusinessException(ErrorBaseCode.AUTO_TRANSFER_CREATE_FAILED);
        }

        Long investTransferId = null;
        if (req.getRatio() > 0) {
            try {
                CoreCreateAutoTransferReq investReq = createCoreReq(childId, parentAccount.getId(),
                        investAccount.getId(),
                        investAmount, req.getTransferDate());
                CoreCreateAutoTransferRes coreInvestRes = coreAutoTransferClient.createAutoTransfer(investReq);

                if (coreInvestRes == null || coreInvestRes.autoTransferId() == null) {
                    throw new BusinessException(ErrorBaseCode.AUTO_TRANSFER_CREATE_FAILED);
                }
                investTransferId = coreInvestRes.autoTransferId();
            } catch (Exception e) {
                // 투자 자동이체 생성 실패 시, 이미 생성된 용돈 자동이체 롤백 (삭제)
                log.error("투자 자동이체 생성 실패로 인한 롤백 수행: allowanceTransferId={}", coreAllowanceRes.autoTransferId());
                try {
                    coreAutoTransferClient.deleteAutoTransfer(coreAllowanceRes.autoTransferId());
                } catch (Exception rollbackEx) {
                    log.error("롤백 실패: allowanceTransferId={}", coreAllowanceRes.autoTransferId(), rollbackEx);
                }
                throw new BusinessException(ErrorBaseCode.AUTO_TRANSFER_CREATE_FAILED);
            }
        }

        saveAutoTransfer(child, allowanceAccount, req, coreAllowanceRes.autoTransferId(), investTransferId);
    }

    /**
     * 자동이체 설정을 수정합니다.
     * <p>
     * 1. 부모 권한 검증
     * 2. 기존 자동이체 설정 조회
     * 3. 사용자 및 계좌 조회
     * 4. 입력값 검증 (비율 등)
     * 5. 금액 재계산
     * 6. Core 뱅킹 API 호출 (용돈 및 투자 자동이체 수정/생성/해지)
     * 7. 자동이체 정보 업데이트 (DB)
     * </p>
     *
     * @param childId 자녀 ID
     * @param req     수정할 자동이체 설정 정보
     * @param ctx     사용자 컨텍스트
     * @return 수정된 자동이체 정보
     * @throws BusinessException 권한 없음, 설정 없음, 잘못된 입력값 등
     */
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

        CoreAllowanceUpdateAutoTransferReq allowanceReq = updateCoreReq(allowanceAmount, req.getTransferDate());
        CoreAllowanceUpdateAutoTransferReq investReq = updateCoreReq(investAmount, req.getTransferDate());

        Long investTransferId = existingTransfer.getInvestBankTransferId();
        Long newInvestTransferId = investTransferId;

        try {
            // 용돈 자동이체 수정
            coreAutoTransferClient.updateAutoTransfer(
                    existingTransfer.getPrimaryBankTransferId(),
                    allowanceReq);

            // 투자 자동이체 처리
            if (investTransferId != null) {
                if (req.getRatio() > 0) {
                    // 투자 업데이트
                    coreAutoTransferClient.updateAutoTransfer(
                            investTransferId,
                            investReq);
                } else {
                    // 투자 해지
                    coreAutoTransferClient.deleteAutoTransfer(investTransferId);
                    newInvestTransferId = null;
                }
            } else if (req.getRatio() > 0) {
                // 신규 투자 자동이체 생성
                CoreCreateAutoTransferReq newInvestReq = createCoreReq(
                        childId, parentAccount.getId(), investAccount.getId(), investAmount, req.getTransferDate());
                CoreCreateAutoTransferRes investRes = coreAutoTransferClient.createAutoTransfer(newInvestReq);

                if (investRes == null || investRes.autoTransferId() == null) {
                    throw new BusinessException(ErrorBaseCode.AUTO_TRANSFER_CREATE_FAILED);
                }
                newInvestTransferId = investRes.autoTransferId();
            }

        } catch (Exception e) {
            log.error("자동이체 수정 중 오류 발생 - 롤백 진행", e);

            // 투자 실패 시 용돈 수정 롤백
            try {
                coreAutoTransferClient.updateAutoTransfer(
                        existingTransfer.getPrimaryBankTransferId(),
                        updateCoreReq(existingTransfer.getTransferAmount(), existingTransfer.getTransferDate()));
            } catch (Exception rollbackEx) {
                log.error("용돈 롤백 실패 - id={}", existingTransfer.getPrimaryBankTransferId(), rollbackEx);
            }

            throw new BusinessException(ErrorBaseCode.AUTO_TRANSFER_UPDATE_FAILED);
        }

        // 모든 Core 작업 성공 시에만 DB 반영
        existingTransfer.updateAutoTransfer(req, newInvestTransferId);
        autoTransferRepository.save(existingTransfer);

        // 응답 리턴
        return AutoTransferRes.of(
                existingTransfer.getId(),
                existingTransfer.getTransferAmount(),
                existingTransfer.getTransferDate(),
                existingTransfer.getRatio());
    }


    @Override
    @Transactional
    public void deleteAutoTransfer(Long autoTransferId, AutoTransferType type) {

        AutoTransfer autoTransfer = autoTransferRepository.findById(autoTransferId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.AUTO_TRANSFER_NOT_FOUND));

        if(type.equals(AutoTransferType.ALLOWANCE) && autoTransfer.getRatio() > 0) { // 비율이 1 이상인데 타입이 용돈인 경우에는 투자도 삭제되도록
            coreAutoTransferClient.deleteAutoTransfer(autoTransfer.getInvestBankTransferId());
        }
        coreAutoTransferClient.deleteAutoTransfer(autoTransfer.getPrimaryBankTransferId());
        autoTransferRepository.delete(autoTransfer);
    }
    
    /**
     * 부모 권한 및 자녀 관계를 검증합니다.
     *
     * @param ctx     사용자 컨텍스트
     * @param childId 자녀 ID
     * @throws BusinessException 부모가 아니거나 자녀가 아닌 경우
     */
    private void validateParentAccess(UserContext ctx, Long childId) {
        if (!Role.PARENT.name().equals(ctx.getRole())) {
            throw new BusinessException(ErrorBaseCode.PARENT_ONLY_FEATURE);
        }
        if (!ctx.getChildren().contains(childId)) {
            throw new BusinessException(ErrorBaseCode.INVALID_CHILD);
        }
    }

    /**
     * 사용자 ID와 계좌 유형으로 계좌를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param type   계좌 유형
     * @return 조회된 계좌
     * @throws BusinessException 계좌를 찾을 수 없는 경우
     */
    private Account getAccount(Long userId, AccountType type) {
        return accountRepository.findByUserIdAndType(userId, type)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));
    }

    /**
     * 투자 비율을 검증합니다.
     *
     * @param ratio         투자 비율 (0~100)
     * @param investAccount 투자 계좌 (비율이 0보다 클 경우 필수)
     * @throws BusinessException 비율이 범위를 벗어나거나 투자 계좌가 없는 경우
     */
    private void validateRatio(Integer ratio, Account investAccount) {
        if (ratio < 0 || ratio > 100) {
            throw new BusinessException(ErrorBaseCode.INVALID_RATIO_VALUE);
        }
        if (ratio > 0 && investAccount == null) {
            throw new BusinessException(ErrorBaseCode.INVEST_ACCOUNT_REQUIRED);
        }
    }

    /**
     * Core 뱅킹 API 요청 객체를 생성합니다.
     *
     * @param childId 자녀 ID
     * @param fromId  출금 계좌 ID
     * @param toId    입금 계좌 ID
     * @param amount  이체 금액
     * @param date    이체일
     * @return CoreAutoTransferReq 객체
     */
    private CoreCreateAutoTransferReq createCoreReq(Long childId, Long fromId, Long toId, BigDecimal amount,
            Integer date) {
        return new CoreCreateAutoTransferReq(childId, fromId, toId, amount, date, "용돈");
    }

    private CoreAllowanceUpdateAutoTransferReq updateCoreReq(BigDecimal amount, Integer date) {
        return new CoreAllowanceUpdateAutoTransferReq(amount, date);
    }

    /**
     * 자동이체 엔티티를 생성하고 저장합니다.
     *
     * @param user      사용자 (자녀)
     * @param account   주 계좌 (용돈 계좌)
     * @param req       자동이체 요청 정보
     * @param primaryId 주 자동이체 ID (Core)
     * @param investId  투자 자동이체 ID (Core, 없을 경우 null)
     * @return 저장된 AutoTransfer 엔티티
     */
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
