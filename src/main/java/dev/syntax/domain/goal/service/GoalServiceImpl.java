package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.GoalCreateReq;
import dev.syntax.domain.goal.dto.GoalCreateRes;
import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.repository.GoalRepository;
import dev.syntax.domain.notification.service.NotificationService;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRelationshipRepository;
import dev.syntax.domain.user.repository.UserRepository;
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
    public GoalCreateRes createGoal(Long userId, GoalCreateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        if (!user.getRole().equals(Role.CHILD)) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
        }

        Goal goal = Goal.builder()
                .user(user)
                .name(req.getName())
                .targetAmount(req.getTargetAmount())
                .monthlyAmount(req.getMonthlyAmount())
                .payDay(req.getPayDay())
                .build();

        goalRepository.save(goal);

        User parent = userRelationshipRepository.findByChild(user)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND))
                .getParent();

        notificationService.sendGoalRequestNotice(parent, user.getName());

        return new GoalCreateRes(goal);
    }
}
