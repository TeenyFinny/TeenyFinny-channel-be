package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.GoalCreateReq;
import dev.syntax.domain.goal.dto.GoalCreateRes;
import dev.syntax.domain.goal.dto.GoalDetailRes;
import dev.syntax.domain.goal.dto.GoalUpdateReq;
import dev.syntax.domain.goal.dto.GoalUpdateRes;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final NotificationService notificationService;

    // 공통 메서드
    /** UserContext 기반 사용자 조회 */
    private User getUserOrThrow(UserContext userContext) {
        return userRepository.findById(userContext.getId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));
    }

    /** goalId로 Goal 조회 */
    private Goal getGoalOrThrow(Long goalId) {
        return goalRepository.findById(goalId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_NOT_FOUND));
    }

    /** 해당 goal이 로그인한 사용자 소유인지 확인 */
    private void validateGoalOwner(User user, Goal goal) {
        if (!goal.getUser().equals(user)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
        }
    }

    /** 상태가 ONGOING인지 확인 */
    private void validateGoalIsOngoing(Goal goal) {
        if (goal.getStatus() != GoalStatus.ONGOING) {
            throw new BusinessException(ErrorBaseCode.GOAL_NOT_ONGOING);
        }
    }

    /** payDay 값 유효성 검사 */
    private void validatePayDay(Integer payDay) {
        if (payDay == null || payDay < 1 || payDay > 31) {
            throw new BusinessException(ErrorBaseCode.GOAL_INVALID_PAYDAY);
        }
    }

    @Override
    @Transactional
    public GoalCreateRes createGoal(UserContext userContext, GoalCreateReq req) {

        User user = getUserOrThrow(userContext);

        if (!user.getRole().equals(Role.CHILD)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
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
    public GoalDetailRes getGoalForUpdate(UserContext userContext, Long goalId) {

        User user = getUserOrThrow(userContext);
        Goal goal = getGoalOrThrow(goalId);

        validateGoalOwner(user, goal);
        validateGoalIsOngoing(goal);

        return new GoalDetailRes(goal);
    }
}
