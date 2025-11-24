package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.GoalCreateReq;
import dev.syntax.domain.goal.dto.GoalCreateRes;

public interface GoalService {

    GoalCreateRes createGoal(Long userId, GoalCreateReq req);
}
