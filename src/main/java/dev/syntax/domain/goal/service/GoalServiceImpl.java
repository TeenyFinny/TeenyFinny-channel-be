package dev.syntax.domain.goal.service;

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

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public GoalCreateRes createGoal(UserContext userContext, GoalCreateReq req) {
        User user = userRepository.findById(userContext.getId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        if (!user.getRole().equals(Role.CHILD)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
        }

        // PENDING(승인 대기) 목표 있는지 검사
        if (goalRepository.existsByUserAndStatus(user, GoalStatus.PENDING)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ALREADY_PENDING);
        }

        // ONGOING(진행중) 목표 있는지 검사
        if (goalRepository.existsByUserAndStatus(user, GoalStatus.ONGOING)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ALREADY_ONGOING);
        }

        Goal goal = Goal.builder()
                .user(user)
                .name(req.getName())
                .targetAmount(req.getTargetAmount())
                .monthlyAmount(req.getMonthlyAmount())
                .payDay(req.getPayDay())
                .build();

        goalRepository.save(goal);

        User parent = userRepository.findById(userContext.getParentId())
                        .orElseThrow(()-> new BusinessException(ErrorBaseCode.GOAL_PARENT_NOT_FOUND));

        notificationService.sendGoalRequestNotice(parent, user.getName());

        return new GoalCreateRes(goal);
    }

    @Override
    @Transactional
    public GoalUpdateRes updateGoal(UserContext userContext, Long goalId, GoalUpdateReq req) {
        User user = userRepository.findById(userContext.getId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_NOT_FOUND));

        if (!goal.getUser().equals(user)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
        }

        if (goal.getStatus() != GoalStatus.ONGOING) {
            throw new BusinessException(ErrorBaseCode.GOAL_NOT_ONGOING);
        }

        Integer newPayDay = req.getPayDay();

        if (newPayDay == null || newPayDay < 1 || newPayDay > 31) {
            throw new BusinessException(ErrorBaseCode.GOAL_INVALID_PAYDAY);
        }

        goal.updatePayDay(newPayDay);

        return new GoalUpdateRes(goal);
    }

    @Override
    public GoalDetailRes getGoalForUpdate(UserContext userContext, Long goalId) {

        User user = userRepository.findById(userContext.getId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.GOAL_NOT_FOUND));

        if (!goal.getUser().equals(user)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
        }

        if (goal.getStatus() != GoalStatus.ONGOING) {
            throw new BusinessException(ErrorBaseCode.GOAL_NOT_ONGOING);
        }

        return new GoalDetailRes(goal);
    }


}
