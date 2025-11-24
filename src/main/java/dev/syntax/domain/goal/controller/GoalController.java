package dev.syntax.domain.goal.controller;

import dev.syntax.domain.goal.dto.*;
import dev.syntax.domain.goal.service.GoalService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/goal")
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public GoalCreateRes createGoal(@CurrentUser UserContext userContext,
                                    @RequestBody GoalCreateReq req
    ) {
        return goalService.createGoal(userContext, req);
    }

    @GetMapping("/{goalId}/edit")
    public GoalInfoRes getGoalForUpdate(
            @CurrentUser UserContext userContext,
            @PathVariable Long goalId
    ) {
        return goalService.getGoalForUpdate(userContext, goalId);
    }

    @PatchMapping("/{goalId}")
    public GoalUpdateRes updateGoal(@CurrentUser UserContext userContext,
                                    @PathVariable Long goalId,
                                    @RequestBody GoalUpdateReq req) {
        return goalService.updateGoal(userContext, goalId, req);
    }

    @PatchMapping("/{goalId}/approve")
    public GoalApproveRes approveGoal(@CurrentUser UserContext userContext,
                                      @PathVariable Long goalId,
                                      @RequestBody GoalApproveReq req) {
        return goalService.approveGoal(userContext, goalId, req.isApprove());
    }

    @GetMapping("/{goalId}")
    public GoalDetailRes getGoalDetail(@CurrentUser UserContext userContext,
                                       @PathVariable Long goalId) {
        return goalService.getGoalDetail(userContext, goalId);
    }

    @PostMapping("/{goalId}/request-cancel")
    public GoalDeleteRes requestCancel(@CurrentUser UserContext userContext,
                                       @PathVariable Long goalId) {
        return goalService.requestCancel(userContext, goalId);
    }

}
