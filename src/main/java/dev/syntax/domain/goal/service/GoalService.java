package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.GoalCreateReq;
import dev.syntax.domain.goal.dto.GoalCreateRes;
import dev.syntax.global.auth.dto.UserContext;

public interface GoalService {

    GoalCreateRes createGoal(UserContext userContext, GoalCreateReq req);
}
