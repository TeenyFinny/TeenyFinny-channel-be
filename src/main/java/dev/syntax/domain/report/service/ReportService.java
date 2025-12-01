package dev.syntax.domain.report.service;

import dev.syntax.domain.report.dto.ReportRes;
import dev.syntax.global.auth.dto.UserContext;

public interface ReportService {
    /**
     * 프론트엔드에서 요구하는 응답 포맷에 맞추어  
     * 총 소비 금액(totalAmount), 전월 대비 증감(comparedAmount, comparedType),  
     * 카테고리별 금액 및 비율(categories) 정보를 포함한 ReportResponse 를 반환한다.
     *
     * @param userId 리포트를 조회하는 사용자(자녀)의 ID
     * @param month  조회 대상 월 (1~12)
     * @param ctx 현재 요청을 수행하는 사용자 정보(권한 및 인증 정보 포함)
     * @return ReportResponse  프론트가 그대로 사용할 수 있는 월별 소비 리포트 데이터
     */ 
    ReportRes getMonthlyReport(Long userId, int year, int month, UserContext ctx);
}
