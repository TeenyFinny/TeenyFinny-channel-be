package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.*;
import dev.syntax.global.auth.dto.UserContext;

public interface GoalService {

    GoalCreateRes createGoal(UserContext userContext, GoalCreateReq req);

    GoalUpdateRes updateGoal(UserContext userContext, Long goalId, GoalUpdateReq req);

    GoalInfoRes getGoalForUpdate(UserContext userContext, Long goalId);

    GoalApproveRes approveGoal(UserContext userContext, Long goalId, boolean approve);
}
