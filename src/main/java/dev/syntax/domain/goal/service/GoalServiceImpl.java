package dev.syntax.domain.goal.service;

import dev.syntax.domain.core.CoreBankingClient;
import dev.syntax.domain.core.dto.GoalAccountInfoDto;
import dev.syntax.domain.goal.dto.*;
import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.enums.GoalStatus;
import dev.syntax.domain.goal.repository.GoalRepository;
import dev.syntax.domain.notification.service.NotificationService;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRelationshipRepository;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final NotificationService notificationService;
    private final CoreBankingClient coreBankingClient;
    private final UserRelationshipRepository userRelationshipRepository;

    // 공통 메서드

    /**
     * UserContext 기반 사용자 조회
     */
    private User getUserOrThrow(UserContext userContext) {
        return userContext.getUser();
    }

    /**
     * goalId로 Goal 조회
     */
    private Goal getGoalOrThrow(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_NOT_FOUND));
    }

    /**
     * 해당 goal이 로그인한 사용자 소유인지 확인
     */
    private void validateGoalOwner(User user, Goal goal) {
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorBaseCode.FORBIDDEN);
        }
    }

    /**
     * 상태가 ONGOING인지 확인
     */
    private void validateGoalIsOngoing(Goal goal) {
        if (goal.getStatus() != GoalStatus.ONGOING) {
            throw new BusinessException(ErrorBaseCode.GOAL_NOT_ONGOING);
        }
    }

    /**
     * payDay 값 유효성 검사
     */
    private void validatePayDay(Integer payDay) {
        if (payDay == null || payDay < 1 || payDay > 31) {
            throw new BusinessException(ErrorBaseCode.GOAL_INVALID_PAYDAY);
        }
    }

    /**
     * 가족 관계 검증
     */
    private void validateParentHasChild(UserContext userContext, Goal goal) {
        if (!userContext.getChildren().contains(goal.getUser().getId())) {
            throw new BusinessException(ErrorBaseCode.GOAL_CHILD_NOT_MATCH);
        }
    }

    /**
     * 목표 달성 검증
     */
    private void validateGoalIsCompleted(Long goalId, Goal goal) {
        GoalAccountInfoDto info = coreBankingClient.getGoalTransactionInfo(goalId);
        BigDecimal current = info.getCurrentAmount();
        BigDecimal target = goal.getTargetAmount();

        if (current.compareTo(target) < 0) {
            throw new BusinessException(ErrorBaseCode.GOAL_NOT_COMPLETED);
        }
    }

    // 서비스 로직
    @Override
    @Transactional
    public GoalCreateRes createGoal(UserContext userContext, GoalCreateReq req) {

        User user = getUserOrThrow(userContext);

        if (!user.getRole().equals(Role.CHILD)) {
            throw new BusinessException(ErrorBaseCode.GOAL_CREATE_FORBIDDEN);
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

        goalRepository.save(goal);

        User parent = userRepository.findById(userContext.getParentId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_PARENT_NOT_FOUND));

        notificationService.sendGoalRequestNotice(parent, user.getName());

        return new GoalCreateRes(goal);
    }

    @Override
    @Transactional
    public GoalUpdateRes updateGoal(UserContext userContext, Long goalId, GoalUpdateReq req) {

        User user = getUserOrThrow(userContext);
        Goal goal = getGoalOrThrow(goalId);

        validateGoalOwner(user, goal);
        validateGoalIsOngoing(goal);
        validatePayDay(req.getPayDay());

        goal.updatePayDay(req.getPayDay());

        return new GoalUpdateRes(goal);
    }

    @Override
    public GoalInfoRes getGoalForUpdate(UserContext userContext, Long goalId) {

        User user = getUserOrThrow(userContext);
        Goal goal = getGoalOrThrow(goalId);

        validateGoalOwner(user, goal);
        validateGoalIsOngoing(goal);

        return new GoalInfoRes(goal);
    }

    @Override
    @Transactional
    public GoalApproveRes approveGoal(UserContext userContext, Long goalId, boolean approve) {
        User user = getUserOrThrow(userContext);

        if (!user.getRole().equals(Role.PARENT)) {
            throw new BusinessException(ErrorBaseCode.GOAL_CREATE_FORBIDDEN);
        }

        Goal goal = getGoalOrThrow(goalId);

        if (goal.getStatus() != GoalStatus.PENDING) {
            throw new BusinessException(ErrorBaseCode.GOAL_ALREADY_DECIDED);
        }

        validateParentHasChild(userContext, goal);

        if (approve) {
            goal.updateStatus(GoalStatus.ONGOING);
        } else {
            goal.updateStatus(GoalStatus.REJECTED);
        }

        return new GoalApproveRes(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalDetailRes getGoalDetail(UserContext userContext, Long goalId) {

        User user = getUserOrThrow(userContext);
        Goal goal = getGoalOrThrow(goalId);

        if (user.getRole() == Role.CHILD) {
            if (!goal.getUser().getId().equals(user.getId())) {
                throw new BusinessException(ErrorBaseCode.FORBIDDEN);
            }
        }

        if (user.getRole() == Role.PARENT) {
            if (!userContext.getChildren().contains(goal.getUser().getId())) {
                throw new BusinessException(ErrorBaseCode.GOAL_CHILD_NOT_MATCH);
            }
        }

        validateGoalIsOngoing(goal);

        GoalAccountInfoDto coreInfo = coreBankingClient.getGoalTransactionInfo(goalId);

        if (goal.getMonthlyAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorBaseCode.GOAL_INVALID_AMOUNT);
        }
        int period = goal.getTargetAmount()
                .divide(goal.getMonthlyAmount(), RoundingMode.CEILING)
                .intValue();

        int progress = 0;
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = coreInfo.getCurrentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(goal.getTargetAmount(), 0, RoundingMode.HALF_UP)  // ★ 반올림해서 정수 변환
                    .intValue();
        }


        return new GoalDetailRes(
                goal.getId(),
                goal.getUser().getId(),
                goal.getName(),
                goal.getTargetAmount(),
                coreInfo.getCurrentAmount(),
                period,
                progress,
                goal.getUser().getName(),
                coreInfo.getDepositAmounts(),
                coreInfo.getDepositTimes()
        );
    }

    @Override
    @Transactional
    public GoalDeleteRes requestCancel(UserContext userContext, Long goalId) {
        User user = getUserOrThrow(userContext);
        Goal goal = getGoalOrThrow(goalId);

        if (!child.getRole().equals(Role.CHILD)) {
            throw new BusinessException(ErrorBaseCode.FORBIDDEN);
        }

        validateGoalOwner(child, goal);
        validateGoalIsOngoing(goal);

        User parent = userRepository.findById(userContext.getParentId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_PARENT_NOT_FOUND));

        notificationService.sendGoalCancelRequestNotice(parent, child.getName());

        return new GoalDeleteRes(goalId, "중도 해지 요청이 부모에게 전달되었습니다.");
    }

    @Override
    @Transactional
    public GoalDeleteRes confirmCancel(UserContext userContext, Long goalId) {
        User parent = getUserOrThrow(userContext);
        if (!parent.getRole().equals(Role.PARENT)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
        }

        Goal goal = getGoalOrThrow(goalId);

        validateParentHasChild(userContext, goal);
        validateGoalIsOngoing(goal);

        goal.updateStatus(GoalStatus.CANCELLED);

        // TODO: CoreBanking 쪽 계좌 상태를 SUSPENDED로 변경
        // coreBankingClient.suspendGoalAccount(goalId);

        return new GoalDeleteRes(goalId, "목표 계좌가 중도 해지되었습니다.");
    }

    @Override
    @Transactional
    public GoalDeleteRes requestComplete(UserContext userContext, Long goalId) {
        User child = getUserOrThrow(userContext);
        if (!child.getRole().equals(Role.CHILD)) {
            throw new BusinessException(ErrorBaseCode.FORBIDDEN);
        }

        Goal goal = getGoalOrThrow(goalId);

        validateGoalOwner(child, goal);
        validateGoalIsOngoing(goal);

        // TODO: 추후 실제 core 서버와 연동
        validateGoalIsCompleted(goalId, goal);

        User parent = userRepository.findById(userContext.getParentId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_PARENT_NOT_FOUND));

        notificationService.sendGoalCompleteRequestNotice(parent, child.getName());

        return new GoalDeleteRes(goalId, "목표 달성 알림이 부모에게 전달되었습니다.");
    }

    @Override
    @Transactional
    public GoalDeleteRes confirmComplete(UserContext userContext, Long goalId) {
        User parent = getUserOrThrow(userContext);
        if (!parent.getRole().equals(Role.PARENT)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
        }

        Goal goal = getGoalOrThrow(goalId);

        validateParentHasChild(userContext, goal);
        validateGoalIsOngoing(goal);
        validateGoalIsCompleted(goalId, goal);

        goal.updateStatus(GoalStatus.COMPLETED);

        // TODO: CoreBanking 쪽 계좌 상태를 CLOSED로 변경
        // coreBankingClient.closeGoalAccount(goalId);

        return new GoalDeleteRes(goal.getId(), "목표가 달성 완료되었습니다!");
    }

}
