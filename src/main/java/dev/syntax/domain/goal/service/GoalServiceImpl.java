package dev.syntax.domain.goal.service;

import static java.util.Optional.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.goal.dto.*;
import dev.syntax.domain.transfer.entity.AutoTransfer;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.transfer.repository.AutoTransferRepository;
import dev.syntax.domain.transfer.service.AutoTransferService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.goal.client.CoreGoalClient;
import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.enums.GoalStatus;
import dev.syntax.domain.goal.repository.GoalRepository;
import dev.syntax.domain.notification.service.NotificationService;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * GoalServiceImpl
 *
 * <p>목표 생성, 수정, 승인, 취소, 완료 등의 비즈니스 로직을 수행합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final NotificationService notificationService;
	private final CoreGoalClient coreGoalClient;
    private static final DateTimeFormatter GOAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
	private final GoalAccountService goalAccountService;
    private final AutoTransferService autoTransferService;
    private final AutoTransferRepository autoTransferRepository;
    private final AccountRepository accountRepository;

    /**
     * UserContext로부터 User 엔티티 조회
     *
     * @param userContext 현재 로그인한 사용자 컨텍스트
     * @return User 엔티티
     */
    private User getUser(UserContext userContext) {
        return userContext.getUser();
    }

    /**
     * goalId로 Goal 조회
     *
     * @param goalId 목표 ID
     * @return Goal 엔티티
     */
    private Goal getGoalOrThrow(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_NOT_FOUND));
    }

    /**
     * 목표의 소유자가 로그인한 사용자와 같은지 검증
     *
     * @param user 로그인 사용자
     * @param goal 목표 엔티티
     */
    private void validateGoalOwner(User user, Goal goal) {
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorBaseCode.FORBIDDEN);
        }
    }

    /**
     * 목표 상태가 ONGOING인지 검증
     *
     * @param goal 목표 엔티티
     */
    private void validateGoalIsOngoing(Goal goal) {
        if (goal.getStatus() != GoalStatus.ONGOING) {
            throw new BusinessException(ErrorBaseCode.GOAL_NOT_ONGOING);
        }
    }

    /**
     * payDay 값이 유효한 범위(1–31)인지 검증
     *
     * @param payDay 납입일
     */
    private void validatePayDay(Integer payDay) {
        if (payDay == null || payDay < 1 || payDay > 31) {
            throw new BusinessException(ErrorBaseCode.GOAL_INVALID_PAYDAY);
        }
    }

    /**
     * 부모가 해당 자녀와 가족 관계인지 검증
     *
     * @param userContext 로그인한 부모 컨텍스트
     * @param goal        자녀 목표
     */
    private void validateParentHasChild(UserContext userContext, Goal goal) {
        if (!userContext.getChildren().contains(goal.getUser().getId())) {
            throw new BusinessException(ErrorBaseCode.GOAL_CHILD_NOT_MATCH);
        }
    }

    /**
     * 목표가 실제로 목표 금액만큼 달성되었는지 Core 서버 데이터로 확인
     *
     * @param goal   목표 엔티티
     */
    private void validateGoalIsCompleted(Goal goal) {
        String accountNo = goal.getAccount().getAccountNo();
        BigDecimal balance = coreGoalClient.getAccountHistory(accountNo).getBalance();
        if (balance.compareTo(goal.getTargetAmount()) < 0) {
            throw new BusinessException(ErrorBaseCode.GOAL_NOT_COMPLETED);
        }
    }

    private void validateGoalIsNotCompleted(Goal goal) {
        String accountNo = goal.getAccount().getAccountNo();
        BigDecimal balance = coreGoalClient.getAccountHistory(accountNo).getBalance();
        if (balance.compareTo(goal.getTargetAmount()) >= 0) {
            throw new BusinessException(ErrorBaseCode.GOAL_IS_COMPLETED);
        }
    }

    /**
     * 부모 엔티티 조회
     *
     * @param userContext 로그인한 사용자 컨텍스트
     * @return User 엔티티
     */
    private User getParent(UserContext userContext) {
        return userRepository.findById(userContext.getParentId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_PARENT_NOT_FOUND));
    }

    /**
     * 목표 생성
     */
    @Override
    @Transactional
    public GoalCreateRes createGoal(UserContext userContext, GoalCreateReq req) {

        User user = getUser(userContext);

        if (user.getRole() != Role.CHILD) {
            throw new BusinessException(ErrorBaseCode.GOAL_REQUEST_FORBIDDEN);
        }
        if (req.getTargetAmount().compareTo(req.getMonthlyAmount()) < 0) {
            throw new BusinessException(ErrorBaseCode.GOAL_INVALID_AMOUNT);
        }
        if (goalRepository.existsByUserAndStatus(user, GoalStatus.PENDING)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ALREADY_PENDING);
        }
        if (goalRepository.existsByUserAndStatus(user, GoalStatus.ONGOING)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ALREADY_ONGOING);
        }

        validatePayDay(req.getPayDay());

		Goal goal = Goal.builder()
			.user(user)
			.name(req.getName())
			.targetAmount(req.getTargetAmount())
			.monthlyAmount(req.getMonthlyAmount())
			.payDay(req.getPayDay())
			.build();

		log.info("Goal 생성: userId={}, goalId={}, goalName={}", user.getId(), goal.getId(), goal.getName());

		goalRepository.save(goal);

		User parent = getParent(userContext);
		notificationService.sendGoalRequestNotice(parent, user.getName());

		return new GoalCreateRes(goal);
	}

	/**
	 * 목표 납입일 수정
	 */
	@Override
	@Transactional
	public GoalUpdateRes updateGoal(UserContext userContext, Long goalId, GoalUpdateReq req) {

		User user = getUser(userContext);
		Goal goal = getGoalOrThrow(goalId);
        AutoTransfer autoTransfer = autoTransferRepository.findByUserIdAndType(user.getId(), AutoTransferType.GOAL)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.AUTO_TRANSFER_NOT_FOUND));

        Integer payDayForUpdate = req.getPayDay();

        validateGoalOwner(user, goal);
		validateGoalIsOngoing(goal);
		validatePayDay(payDayForUpdate);

        goal.updatePayDay(payDayForUpdate);
		autoTransfer.updateAutoTransferDay(payDayForUpdate);

        coreGoalClient.updateAutoTransferDay(autoTransfer.getPrimaryBankTransferId(), payDayForUpdate);

		return new GoalUpdateRes(goal);
	}

	/**
	 * 목표 수정용 정보 조회
	 */
	@Override
	public GoalInfoRes getGoalForUpdate(UserContext userContext, Long goalId) {

		User user = getUser(userContext);
		Goal goal = getGoalOrThrow(goalId);

		validateGoalOwner(user, goal);
		validateGoalIsOngoing(goal);

		return new GoalInfoRes(goal);
	}

	/**
	 * 목표 승인/반려
	 */
	@Override
	@Transactional
	public GoalApproveRes approveGoal(UserContext userContext, Long goalId, boolean approve) {

		User user = getUser(userContext);

		if (user.getRole() != Role.PARENT) {
			throw new BusinessException(ErrorBaseCode.GOAL_REQUEST_FORBIDDEN);
		}

		Goal goal = getGoalOrThrow(goalId);
		if (goal.getStatus() != GoalStatus.PENDING) {
			throw new BusinessException(ErrorBaseCode.GOAL_ALREADY_DECIDED);
		}

		validateParentHasChild(userContext, goal);

		goal.updateStatus(approve ? GoalStatus.ONGOING : GoalStatus.REJECTED);

		if (approve) {
			// core에 계좌 생성 요청
			Goal createdGoal = goalAccountService.createGoalAccount(goal);
			return new GoalApproveRes(createdGoal);
		}

		return new GoalApproveRes(goal);
	}

	/**
	 * 목표 상세 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public GoalDetailRes getGoalDetail(UserContext userContext, Long goalId) {

		User user = getUser(userContext);
		Goal goal = getGoalOrThrow(goalId);

		if (user.getRole() == Role.CHILD && !goal.getUser().getId().equals(user.getId())) {
			throw new BusinessException(ErrorBaseCode.FORBIDDEN);
		}
		if (user.getRole() == Role.PARENT && !userContext.getChildren().contains(goal.getUser().getId())) {
			throw new BusinessException(ErrorBaseCode.GOAL_CHILD_NOT_MATCH);
		}

		validateGoalIsOngoing(goal);

		String accountNo = ofNullable(goal.getAccount())
			.map(Account::getAccountNo)
			.orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

		CoreTransactionHistoryRes history =
			coreGoalClient.getAccountHistory(accountNo);

		if (history == null) {
			throw new BusinessException(ErrorBaseCode.CORE_API_ERROR);
		}

		BigDecimal currentAmount = history.getBalance();

		// 거래 내역 조회
		List<CoreTransactionHistoryRes.TransactionItem> depositTransactions = history.getTransactions().stream()
			.filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) > 0)
			.toList();
		List<BigDecimal> depositAmounts = depositTransactions.stream()
			.map(CoreTransactionHistoryRes.TransactionItem::getAmount)
			.toList();
		List<String> depositDates = depositTransactions.stream()
			.map(t -> t.getTransactionDate().format(GOAL_DATE_FORMATTER))
			.toList();

		// 목표 기간 계산
		int period = depositTransactions.size();
		
		// 진행률 계산
		int progress = 0;
		if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
			progress = currentAmount
				.multiply(BigDecimal.valueOf(100))
				.divide(goal.getTargetAmount(), 0, RoundingMode.HALF_UP)
				.intValue();
		}

		return new GoalDetailRes(
			goal.getId(),
			goal.getUser().getId(),
			goal.getName(),
			goal.getTargetAmount(),
			currentAmount,
			period,
			progress,
			goal.getUser().getName(),
			depositAmounts,
			depositDates
		);
	}

	/**
	 * 목표 중도해지 요청
	 */
	@Override
	@Transactional
	public GoalDeleteRes requestCancel(UserContext userContext, Long goalId) {

		User user = getUser(userContext);
		Goal goal = getGoalOrThrow(goalId);

		if (user.getRole() != Role.CHILD) {
			throw new BusinessException(ErrorBaseCode.GOAL_REQUEST_FORBIDDEN);
		}

		validateGoalOwner(user, goal);
		validateGoalIsOngoing(goal);
		validateGoalIsNotCompleted(goal);

		User parent = getParent(userContext);
		notificationService.sendGoalCancelRequestNotice(parent, user.getName(), goal.getName());

		return new GoalDeleteRes(goalId, "중도 해지 요청이 부모에게 전달되었습니다.");
	}

	/**
	 * 목표 중도해지 확정
	 */
	@Override
	@Transactional
	public GoalDeleteRes confirmCancel(UserContext userContext, Long goalId) {

		User parent = getUser(userContext);
		if (parent.getRole() != Role.PARENT) {
			throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
		}

        Goal goal = getGoalOrThrow(goalId);
        String accountNo = goal.getAccount().getAccountNo();
        Account allowanceAccount = accountRepository.findByUserIdAndType(goal.getUser().getId(), AccountType.ALLOWANCE)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        validateParentHasChild(userContext, goal);
        validateGoalIsOngoing(goal);
        validateGoalIsNotCompleted(goal);

        goal.updateStatus(GoalStatus.CANCELLED);
        coreGoalClient.updateAccountStatus(accountNo, new CoreUpdateAccountStatusReq("SUSPENDED"));
        autoTransferService.deleteAutoTransfer(allowanceAccount.getId(), AutoTransferType.GOAL);

		return new GoalDeleteRes(goalId, "목표 계좌가 중도 해지되었습니다.");
	}

	/**
	 * 목표 완료 요청
	 */
	@Override
	@Transactional
	public GoalDeleteRes requestComplete(UserContext userContext, Long goalId) {

		User child = getUser(userContext);
		if (child.getRole() != Role.CHILD) {
			throw new BusinessException(ErrorBaseCode.GOAL_REQUEST_FORBIDDEN);
		}

		Goal goal = getGoalOrThrow(goalId);

        validateGoalOwner(child, goal);
        validateGoalIsOngoing(goal);
        validateGoalIsCompleted(goal);

		User parent = getParent(userContext);
		notificationService.sendGoalCompleteRequestNotice(parent, child.getName());

		return new GoalDeleteRes(goalId, "목표 달성 알림이 부모에게 전달되었습니다.");
	}

	/**
	 * 목표 완료 확정
	 */
	@Override
	@Transactional
	public GoalDeleteRes confirmComplete(UserContext userContext, Long goalId) {

		User parent = getUser(userContext);
		if (parent.getRole() != Role.PARENT) {
			throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
		}

        Goal goal = getGoalOrThrow(goalId);
        String goalAccountNo = goal.getAccount().getAccountNo();
        Account allowanceAccount = accountRepository.findByUserIdAndType(goal.getUser().getId(), AccountType.ALLOWANCE)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        validateParentHasChild(userContext, goal);
        validateGoalIsOngoing(goal);
        validateGoalIsCompleted(goal);

        // 채널 목표 상태 변경
        goal.updateStatus(GoalStatus.COMPLETED);

        // 코어 목표 적금 계좌 상태 변경
        coreGoalClient.updateAccountStatus(goalAccountNo, new CoreUpdateAccountStatusReq("CLOSED"));

        // 채널 + 코어 자동 이체 삭제
        autoTransferService.deleteAutoTransfer(allowanceAccount.getId(), AutoTransferType.GOAL);

		return new GoalDeleteRes(goal.getId(), "목표가 달성 완료되었습니다!");
	}

//    @Override
//    @Transactional
//    public void handleTransactionEvent(dev.syntax.domain.goal.dto.GoalTransactionEventReq req) {
//        log.info("Handling transaction event for account: {}", req.getAccountNo());
//
//        goalRepository.findByAccount_AccountNo(req.getAccountNo()).ifPresent(goal -> {
//            if (goal.getStatus() == GoalStatus.ONGOING) {
//                if (req.getBalanceAfter().compareTo(goal.getTargetAmount()) >= 0) {
//                    log.info("Goal achieved for user: {}, goal: {}", goal.getUser().getId(), goal.getName());
//                    notificationService.sendGoalAchievedNotice(goal.getUser(), goal.getName());
//                }
//            }
//        });
//    }

    @Override
    @Transactional(readOnly = true)
    public Long getOngoingGoalId(UserContext userContext, Long childId) {
        User parent = getUser(userContext);
        if (parent.getRole() != Role.PARENT) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
        }

        if (!userContext.getChildren().contains(childId)) {
            throw new BusinessException(ErrorBaseCode.GOAL_CHILD_NOT_MATCH);
        }

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        Goal goal = goalRepository.findByUserAndStatus(child, GoalStatus.ONGOING)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_NOT_ONGOING));

        return goal.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public GoalPendingRes getPendingGoal(UserContext userContext, Long childId) {
        User parent = getUser(userContext);
        if (parent.getRole() != Role.PARENT) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
        }

        if (!userContext.getChildren().contains(childId)) {
            throw new BusinessException(ErrorBaseCode.GOAL_CHILD_NOT_MATCH);
        }

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        Goal goal = goalRepository.findByUserAndStatus(child, GoalStatus.PENDING)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_NOT_FOUND));

        return new GoalPendingRes(goal);
    }
    @Override
    @Transactional(readOnly = true)
    public Long getMyOngoingGoalId(UserContext userContext) {
        User user = getUser(userContext);
        Goal goal = goalRepository.findByUserAndStatus(user, GoalStatus.ONGOING)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_NOT_ONGOING));
        return goal.getId();
    }
}
