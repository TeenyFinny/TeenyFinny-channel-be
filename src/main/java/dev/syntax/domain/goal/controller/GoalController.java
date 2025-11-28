package dev.syntax.domain.goal.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import dev.syntax.domain.goal.dto.GoalApproveReq;
import dev.syntax.domain.goal.dto.GoalApproveRes;
import dev.syntax.domain.goal.dto.GoalCreateReq;
import dev.syntax.domain.goal.dto.GoalCreateRes;
import dev.syntax.domain.goal.dto.GoalDeleteRes;
import dev.syntax.domain.goal.dto.GoalDetailRes;
import dev.syntax.domain.goal.dto.GoalInfoRes;
import dev.syntax.domain.goal.dto.GoalUpdateReq;
import dev.syntax.domain.goal.dto.GoalUpdateRes;
import dev.syntax.domain.goal.service.GoalService;
import dev.syntax.global.auth.annotation.CurrentUser;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.response.ApiResponseUtil;
import dev.syntax.global.response.BaseResponse;
import dev.syntax.global.response.SuccessCode;
import lombok.RequiredArgsConstructor;

/**
 * GoalController
 *
 * <p>목표(Goal) 관련 API를 제공하는 컨트롤러입니다.
 * 목표 생성, 상세 조회, 수정, 승인 및
 * 목표 취소/완료 요청 및 확정 기능을 제공합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/goal")
public class GoalController {

	private final GoalService goalService;

	/**
	 * 목표 생성 API
	 *
	 * @param userContext 현재 로그인한 사용자 정보 (자녀)
	 * @param req         목표 생성 요청 DTO
	 * @return 생성된 목표 정보(GoalCreateRes)를 포함한 성공 응답
	 */
	@PostMapping
	public ResponseEntity<BaseResponse<?>> createGoal(
		@CurrentUser UserContext userContext,
		@RequestBody GoalCreateReq req
	) {
		GoalCreateRes result = goalService.createGoal(userContext, req);
		return ApiResponseUtil.success(SuccessCode.CREATED, result);
	}

	/**
	 * 목표 수정용 정보 조회 API
	 *
	 * <p>목표 수정 화면에서 사용할 정보를 조회합니다.</p>
	 *
	 * @param userContext 현재 로그인한 사용자 정보
	 * @param goalId      조회할 목표 ID
	 * @return 목표 수정용 정보(GoalInfoRes)를 포함한 성공 응답
	 */
	@GetMapping("/{goalId}/edit")
	public ResponseEntity<BaseResponse<?>> getGoalForUpdate(
		@CurrentUser UserContext userContext,
		@PathVariable Long goalId
	) {
		GoalInfoRes result = goalService.getGoalForUpdate(userContext, goalId);
		return ApiResponseUtil.success(SuccessCode.OK, result);
	}

	/**
	 * 목표 수정 API
	 *
	 * @param userContext 현재 로그인한 사용자 정보
	 * @param goalId      수정할 목표 ID
	 * @param req         목표 수정 요청 DTO
	 * @return 수정된 목표 정보(GoalUpdateRes)를 포함한 성공 응답
	 */
	@PatchMapping("/{goalId}")
	public ResponseEntity<BaseResponse<?>> updateGoal(
		@CurrentUser UserContext userContext,
		@PathVariable Long goalId,
		@RequestBody GoalUpdateReq req
	) {
		GoalUpdateRes result = goalService.updateGoal(userContext, goalId, req);
		return ApiResponseUtil.success(SuccessCode.OK, result);
	}

	/**
	 * 목표 승인/반려 API
	 *
	 * <p>부모가 자녀의 목표에 대해 승인 또는 반려합니다.</p>
	 *
	 * @param userContext 현재 로그인한 사용자 정보
	 * @param goalId      승인/반려할 목표 ID
	 * @param req         승인 여부가 담긴 요청 DTO
	 * @return 승인/반려 결과(GoalApproveRes)를 포함한 성공 응답
	 */
	@PatchMapping("/{goalId}/approve")
	public ResponseEntity<BaseResponse<?>> approveGoal(
		@CurrentUser UserContext userContext,
		@PathVariable Long goalId,
		@RequestBody GoalApproveReq req
	) {
		GoalApproveRes result = goalService.approveGoal(userContext, goalId, req.isApprove());
		return ApiResponseUtil.success(SuccessCode.OK, result);
	}

	/**
	 * 목표 상세 조회 API
	 *
	 * @param userContext 현재 로그인한 사용자 정보
	 * @param goalId      조회할 목표 ID
	 * @return 목표 상세 정보(GoalDetailRes)를 포함한 성공 응답
	 */
	@GetMapping("/{goalId}")
	public ResponseEntity<BaseResponse<?>> getGoalDetail(
		@CurrentUser UserContext userContext,
		@PathVariable Long goalId
	) {
		GoalDetailRes result = goalService.getGoalDetail(userContext, goalId);
		return ApiResponseUtil.success(SuccessCode.OK, result);
	}

	/**
	 * 목표 취소 요청 API
	 *
	 * <p>자녀(또는 요청 권한이 있는 사용자)가 목표 취소를 요청합니다.</p>
	 *
	 * @param userContext 현재 로그인한 사용자 정보
	 * @param goalId      취소 요청할 목표 ID
	 * @return 목표 취소 요청 결과(GoalDeleteRes)를 포함한 성공 응답
	 */
	@PostMapping("/{goalId}/request-cancel")
	public ResponseEntity<BaseResponse<?>> requestCancel(
		@CurrentUser UserContext userContext,
		@PathVariable Long goalId
	) {
		GoalDeleteRes result = goalService.requestCancel(userContext, goalId);
		return ApiResponseUtil.success(SuccessCode.OK, result);
	}

    /**
     * 목표 취소 확정 API
     *
     * <p>부모가 자녀의 목표 취소 요청을 승인(확정)합니다.</p>
     *
     * @param userContext 현재 로그인한 사용자 정보
     * @param goalId      취소 확정할 목표 ID
     * @return 목표 취소 확정 결과(GoalDeleteRes)를 포함한 성공 응답
     */
    @PutMapping("/{goalId}/confirm-cancel")
    public ResponseEntity<BaseResponse<?>> confirmCancel(
            @CurrentUser UserContext userContext,
            @PathVariable Long goalId
    ) {
        GoalDeleteRes result = goalService.confirmCancel(userContext, goalId);
        return ApiResponseUtil.success(SuccessCode.OK, result);
    }

	/**
	 * 목표 완료 요청 API
	 *
	 * <p>자녀(또는 요청 권한이 있는 사용자)가 목표 완료를 요청합니다.</p>
	 *
	 * @param userContext 현재 로그인한 사용자 정보
	 * @param goalId      완료 요청할 목표 ID
	 * @return 목표 완료 요청 결과(GoalDeleteRes 또는 상태 DTO)를 포함한 성공 응답
	 */
	@PostMapping("/{goalId}/request-complete")
	public ResponseEntity<BaseResponse<?>> requestComplete(
		@CurrentUser UserContext userContext,
		@PathVariable Long goalId
	) {
		GoalDeleteRes result = goalService.requestComplete(userContext, goalId);
		return ApiResponseUtil.success(SuccessCode.OK, result);
	}

    /**
     * 목표 완료 확정 API
     *
     * <p>부모가 자녀의 목표 완료 요청을 승인(확정)합니다.</p>
     *
     * @param userContext 현재 로그인한 사용자 정보
     * @param goalId      완료 확정할 목표 ID
     * @return 목표 완료 확정 결과(GoalDeleteRes 또는 상태 DTO)를 포함한 성공 응답
     */
    @PutMapping("/{goalId}/confirm-complete")
    public ResponseEntity<BaseResponse<?>> confirmComplete(
            @CurrentUser UserContext userContext,
            @PathVariable Long goalId
    ) {
        GoalDeleteRes result = goalService.confirmComplete(userContext, goalId);
        return ApiResponseUtil.success(SuccessCode.OK, result);
    }

    /**
     * 거래 이벤트 수신 API (Core -> Channel)
     *
     * <p>Core 시스템에서 거래가 발생했을 때 호출되는 웹훅 엔드포인트입니다.
     * 목표 계좌의 입금으로 인해 목표 금액이 달성되었는지 확인하고 알림을 발송합니다.</p>
     *
     * @param req 거래 이벤트 정보 (계좌번호, 잔액 등)
     * @return 성공 응답
     */
//    @PostMapping("/transaction-event")
//    public ResponseEntity<BaseResponse<?>> handleTransactionEvent(
//            @RequestBody dev.syntax.domain.goal.dto.GoalTransactionEventReq req
//    ) {
//        goalService.handleTransactionEvent(req);
//        return ApiResponseUtil.success(SuccessCode.OK);
//    }

    /**
     * 자녀의 진행 중인 목표 ID 조회 API
     *
     * <p>부모가 자녀의 진행 중인 목표 ID를 조회합니다.
     * 알림 클릭 시 해당 목표로 이동하거나 작업을 수행하기 위해 사용됩니다.</p>
     *
     * @param userContext 현재 로그인한 사용자 정보 (부모)
     * @param childId     조회할 자녀 ID
     * @return 진행 중인 목표 ID
     */
    @GetMapping("/child/{childId}/ongoing")
    public ResponseEntity<BaseResponse<?>> getOngoingGoalId(
            @CurrentUser UserContext userContext,
            @PathVariable Long childId
    ) {
        Long goalId = goalService.getOngoingGoalId(userContext, childId);
        return ApiResponseUtil.success(SuccessCode.OK, goalId);
    }
}
