package dev.syntax.domain.goal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreGoalAccountReq;
import dev.syntax.domain.account.dto.core.CoreUserAccountListRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.repository.GoalRepository;
import dev.syntax.domain.transfer.client.CoreAutoTransferClient;
import dev.syntax.domain.transfer.dto.CoreCreateAutoTransferReq;
import dev.syntax.domain.transfer.dto.CoreCreateAutoTransferRes;
import dev.syntax.domain.transfer.entity.AutoTransfer;
import dev.syntax.domain.transfer.enums.AutoTransferFrequency;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.transfer.repository.AutoTransferRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * GoalAccountServiceImpl
 *
 * <p>사용자 목표 계좌 생성 관련 비즈니스 로직을 수행하는 서비스입니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalAccountServiceImpl implements GoalAccountService {

	private final CoreAccountClient coreAccountClient;
	private final AccountRepository accountRepository;
	private final AutoTransferRepository autoTransferRepository;
	private final GoalRepository goalRepository;
	private final CoreAutoTransferClient coreAutoTransferClient;

	/**
	 * 목표 계좌를 생성하고 Goal 엔티티와 연동합니다.
	 *
	 * <p>주요 처리 과정:</p>
	 * <ol>
	 *     <li>Core 서버에 목표 계좌 생성 요청</li>
	 *     <li>생성된 계좌 정보를 Channel DB에 저장</li>
	 *     <li>저장된 계좌를 Goal 엔티티와 연동</li>
	 *     <li>자녀 용돈 계좌 → 목표 계좌 자동이체 등록 (별도 private 메소드에서 처리)</li>
	 * </ol>
	 *
	 * <p>자동이체 등록 과정은 registerAutoTransfer() 내부에서 다음을 수행합니다:</p>
	 * <ul>
	 *     <li>자녀 용돈 계좌 조회</li>
	 *     <li>Core 자동이체 등록 요청</li>
	 *     <li>자동이체 정보를 Channel DB에 저장</li>
	 *     <li>Channel 저장 실패 시 Core 자동이체 롤백</li>
	 * </ul>
	 *
	 * @param goal 목표 엔티티
	 * @return 계좌 생성 및 자동이체 연동이 완료된 Goal 엔티티
	 */

	@Transactional
	public Goal createGoalAccount(Goal goal) {
		// 1. Core 목표 계좌 생성 요청
		CoreGoalAccountReq req = CoreGoalAccountReq.builder()
			.childCoreId(goal.getUser().getCoreUserId())
			.name(goal.getName())
			.build();

		CoreAccountItemRes coreRes = coreAccountClient.createGoalAccount(req);
		log.info("[CORE] 목표 계좌 생성 완료: userId={}, accountNumber={}",
			goal.getUser().getId(), coreRes.accountNumber());

		if (coreRes.accountNumber() == null) {
			throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
		}

		// 2. 채널 DB 계좌 저장
		Account account = Account.builder()
			.user(goal.getUser())
			.type(AccountType.GOAL)
			.accountNo(coreRes.accountNumber())
			.build();
		accountRepository.save(account);

		// 3. Goal과 계좌 연동
		goal.updateAccount(account);
		goalRepository.save(goal);

		// 4. 자동이체 등록 (private 메소드로 분리)
		registerAutoTransfer(goal, account);

		log.info("[CHANNEL] 목표 계좌 생성 완료: userId={}, goalId={}, accountId={}, goalName={}",
			goal.getUser().getId(), goal.getId(), account.getId(), goal.getName());

		return goal;
	}

	// 자동 이체 플로우
	private void registerAutoTransfer(Goal goal, Account goalAccount) {
		// 1. 용돈 계좌 조회
		Account allowanceAccount = findAllowanceAccount(goal);

		// 2. Core 자동이체 등록
		CoreCreateAutoTransferRes transferRes =
			createAutoTransferOnCore(goal, goalAccount, allowanceAccount);

		// 3. Channel DB 자동이체 저장
		saveAutoTransferChannel(goal, goalAccount, transferRes);
	}

	// 용돈 계좌 조회
	private Account findAllowanceAccount(Goal goal) {
		return accountRepository.findByUserIdAndType(goal.getUser().getId(), AccountType.ALLOWANCE)
			.orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));
	}

	// core 자동이체 등록
	private CoreCreateAutoTransferRes createAutoTransferOnCore(
		Goal goal, Account goalAccount, Account allowanceAccount) {

		CoreCreateAutoTransferReq autoTransferReq = CoreCreateAutoTransferReq.builder()
			.userId(goal.getUser().getCoreUserId())
			.fromAccountId(allowanceAccount.getId())
			.toAccountId(goalAccount.getId())
			.amount(goal.getMonthlyAmount())
			.transferDay(goal.getPayDay())
			.memo(AccountType.GOAL.name())
			.build();

		CoreCreateAutoTransferRes transferRes = coreAutoTransferClient.createAutoTransfer(autoTransferReq);

		if (transferRes == null || transferRes.autoTransferId() == null) {
			throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
		}

		log.info("[CORE] 목표 자동이체 등록 완료: userId={}, autoTransferId={}",
			goal.getUser().getId(), transferRes.autoTransferId());

		return transferRes;
	}

	// channel DB에 저장
	private void saveAutoTransferChannel(
		Goal goal, Account goalAccount, CoreCreateAutoTransferRes transferRes) {

		try {
			AutoTransfer autoTransfer = AutoTransfer.builder()
				.user(goal.getUser())
				.account(goalAccount)
				.transferAmount(goal.getMonthlyAmount())
				.type(AutoTransferType.GOAL)
				.frequency(AutoTransferFrequency.MONTHLY)
				.primaryBankTransferId(transferRes.autoTransferId())
				.transferDate(goal.getPayDay())
				.build();

			autoTransferRepository.save(autoTransfer);

		} catch (Exception e) {
			log.warn("[CHANNEL] 채널 DB 자동이체 저장 실패. Core에 계좌 존재 여부 확인 후 재시도. autoTransferId={}",
				transferRes.autoTransferId());

			// Core에 계좌가 실제로 존재하는지 확인
			boolean accountExistsInCore = verifyAccountExistsInCore(goalAccount.getAccountNo());

			if (accountExistsInCore) {
				log.info("[CHANNEL] Core에 계좌 존재 확인. 채널 DB 재등록 시도. accountNo={}",
					goalAccount.getAccountNo());

				// 재시도
				try {
					AutoTransfer autoTransfer = AutoTransfer.builder()
						.user(goal.getUser())
						.account(goalAccount)
						.transferAmount(goal.getMonthlyAmount())
						.type(AutoTransferType.GOAL)
						.frequency(AutoTransferFrequency.MONTHLY)
						.primaryBankTransferId(transferRes.autoTransferId())
						.transferDate(goal.getPayDay())
						.build();

					autoTransferRepository.save(autoTransfer);
					log.info("[CHANNEL] 채널 DB 재등록 성공. autoTransferId={}", transferRes.autoTransferId());
				} catch (Exception retryException) {
					log.error("[CHANNEL] 채널 DB 재등록 실패. Core 자동이체 롤백 시도. autoTransferId={}",
						transferRes.autoTransferId(), retryException);

					coreAutoTransferClient.deleteAutoTransfer(transferRes.autoTransferId());

					throw new BusinessException(ErrorBaseCode.AUTO_TRANSFER_SAVE_FAILED);
				}
			} else {
				log.warn("[CORE] Core에 계좌 미존재. Core 자동이체 롤백 시도. autoTransferId={}",
					transferRes.autoTransferId());
				coreAutoTransferClient.deleteAutoTransfer(transferRes.autoTransferId());
				throw new BusinessException(ErrorBaseCode.AUTO_TRANSFER_SAVE_FAILED);
			}
		}
	}

	/**
	 * Core 서버에 계좌가 존재하는지 확인합니다.
	 *
	 * @param accountNo 확인할 계좌 번호
	 * @return 계좌 존재 여부
	 */
	private boolean verifyAccountExistsInCore(String accountNo) {
		try {
			CoreUserAccountListRes accountList = coreAccountClient.getUserAccounts();

			// 모든 계좌 목록에서 해당 계좌 번호가 있는지 확인
			return accountList.accounts().stream()
				.anyMatch(account -> account.accountNumber().equals(accountNo));
		} catch (Exception e) {
			log.error("[CORE] Core 목표 계좌 조회 실패. accountNo={}", accountNo, e);
			return false; // 조회 실패 시 안전하게 false 반환
		}
	}
}