package dev.syntax.domain.goal.enums;

/**
 * GoalStatus
 *
 * <p>목표의 현재 상태를 나타내는 Enum 값입니다.<br>
 * 자녀가 목표를 생성하고 부모가 승인하는 구조의 적금 목표 프로세스를 표현합니다.</p>
 */
public enum GoalStatus {

    /**
     * 자녀가 목표를 생성하고 부모의 승인을 기다리는 상태
     */
    PENDING,

    /**
     * 부모가 목표를 승인하지 않아 반려된 상태
     */
    REJECTED,

    /**
     * 목표가 승인되어 적금이 진행 중인 상태
     */
    ONGOING,

    /**
     * 목표 금액을 달성하여 완료된 상태
     */
    COMPLETED,

    /**
     * 목표 적금이 중도 해지된 상태
     */
    CANCELLED
}
