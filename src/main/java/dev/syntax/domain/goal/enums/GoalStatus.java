package dev.syntax.domain.goal.enums;

public enum GoalStatus {
    PENDING, // 아이가 요청 -> 부모 승인 대기
    REJECTED, // 부모가 거절했을 때
    ONGOING, // 부모 승인 -> 목표 적금 진행 중
    COMPLETED, // 목표 달성 완료
    CANCELLED // 증도 해지(취소)
}