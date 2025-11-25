package dev.syntax.domain.goal.service;

import dev.syntax.domain.goal.dto.*;
import dev.syntax.global.auth.dto.UserContext;

/**
 * GoalService
 *
 * <p>목표(Goal) 기능을 처리하는 서비스 인터페이스입니다.<br>
 * 목표 생성, 수정, 승인, 상세 조회, 취소, 완료 등
 * 목표와 관련된 모든 비즈니스 로직의 진입점을 정의합니다.</p>
 */
public interface GoalService {

    /**
     * 목표 생성
     *
     * @param userContext 현재 로그인한 사용자 정보(자녀)
     * @param req 목표 생성 요청 DTO
     * @return 생성된 목표 정보
     */
    GoalCreateRes createGoal(UserContext userContext, GoalCreateReq req);

    /**
     * 목표 납입일 수정
     *
     * @param userContext 현재 로그인한 사용자 정보
     * @param goalId 수정할 목표 ID
     * @param req 목표 수정 요청 DTO
     * @return 수정된 목표 정보
     */
    GoalUpdateRes updateGoal(UserContext userContext, Long goalId, GoalUpdateReq req);

    /**
     * 목표 수정용 정보 조회
     *
     * @param userContext 현재 로그인한 사용자 정보
     * @param goalId 조회할 목표 ID
     * @return 목표 수정 페이지에서 사용될 정보
     */
    GoalInfoRes getGoalForUpdate(UserContext userContext, Long goalId);

    /**
     * 목표 승인 또는 반려
     *
     * @param userContext 현재 로그인한 사용자 정보(부모)
     * @param goalId 승인/반려할 목표 ID
     * @param approve true → 승인, false → 반려
     * @return 승인/반려 후 목표 상태
     */
    GoalApproveRes approveGoal(UserContext userContext, Long goalId, boolean approve);

    /**
     * 목표 상세 조회
     *
     * @param userContext 현재 로그인한 사용자 정보
     * @param goalId 조회할 목표 ID
     * @return 목표 상세 정보
     */
    GoalDetailRes getGoalDetail(UserContext userContext, Long goalId);

    /**
     * 목표 중도해지 요청
     *
     * @param userContext 현재 로그인한 사용자 정보(자녀)
     * @param goalId 해지 요청할 목표 ID
     * @return 목표 중도해지 요청 결과
     */
    GoalDeleteRes requestCancel(UserContext userContext, Long goalId);

    /**
     * 목표 중도해지 확정
     *
     * @param userContext 현재 사용자 정보(부모)
     * @param goalId 중도해지를 확정할 목표 ID
     * @return 목표 중도해지 확정 결과
     */
    GoalDeleteRes confirmCancel(UserContext userContext, Long goalId);

    /**
     * 목표 완료 요청
     *
     * @param userContext 현재 사용자 정보(자녀)
     * @param goalId 목표 완료 요청할 ID
     * @return 목표 완료 요청 결과
     */
    GoalDeleteRes requestComplete(UserContext userContext, Long goalId);

    /**
     * 목표 완료 확정
     *
     * @param userContext 현재 사용자 정보(부모)
     * @param goalId 완료 확정할 목표 ID
     * @return 목표 완료 확정 결과
     */
    GoalDeleteRes confirmComplete(UserContext userContext, Long goalId);
}
