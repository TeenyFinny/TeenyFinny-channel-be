package dev.syntax.domain.report.scheduler;

import dev.syntax.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private final ReportService reportService;

    /**
     * 매월 1일 0시 0분 0초에 실행
     * 1. 1년 지난 리포트 삭제
     */
    @Scheduled(cron = "0 0 0 1 * *")
    public void runMonthlyReportJob() {
        LocalDate now = LocalDate.now();
        log.info("[ReportScheduler] 월간 리포트 배치 작업 시작 - 기준일: {}", now);

        // 1. 1년 전 데이터 삭제
        try {
            reportService.deleteOldReports(now.getYear(), now.getMonthValue());
        } catch (Exception e) {
            log.error("[ReportScheduler] 리포트 삭제 중 오류 발생", e);
        }
        
        log.info("[ReportScheduler] 월간 리포트 배치 작업 종료");
    }
}
