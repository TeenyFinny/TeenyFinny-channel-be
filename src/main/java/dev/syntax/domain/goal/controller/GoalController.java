package dev.syntax.domain.goal.controller;

import dev.syntax.domain.goal.dto.GoalCreateReq;
import dev.syntax.domain.goal.dto.GoalCreateRes;
import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.service.GoalService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/goal")
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public GoalCreateRes createGoal(@CurrentUser UserContext userContext,
                                    @RequestBody GoalCreateReq req
    ) {
        return goalService.createGoal(userContext.getId(), req);
    }
}
