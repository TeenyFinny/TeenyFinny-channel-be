package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.GoalCreateReq;
import dev.syntax.domain.goal.dto.GoalCreateRes;
import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.repository.GoalRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;

    @Override
    public GoalCreateRes createGoal(Long userId, GoalCreateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        if (!user.getRole().name().equals("CHILD")) {
            throw new BusinessException(ErrorBaseCode.GOAL_ACCESS_FORBIDDEN);
        }

        if (req.getTargetAmount().compareTo(req.getMonthlyAmount()) < 0) {
            throw new BusinessException(ErrorBaseCode.GOAL_INVALID_AMOUNT);
        }

        if (req.getPayDay() < 1 || req.getPayDay() > 31) {
            throw new BusinessException(ErrorBaseCode.GOAL_INVALID_PAYDAY);
        }

        Goal goal = Goal.builder()
                .user(user)
                .name(req.getName())
                .targetAmount(req.getTargetAmount())
                .monthlyAmount(req.getMonthlyAmount())
                .payDay(req.getPayDay())
                .build();

        goalRepository.save(goal);

        return new GoalCreateRes(goal);
    }
}
