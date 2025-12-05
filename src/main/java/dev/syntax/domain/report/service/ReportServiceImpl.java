package dev.syntax.domain.report.service;

import dev.syntax.domain.account.client.CoreAccountClient;

import dev.syntax.domain.account.dto.core.CoreTransactionHistoryRes;
import dev.syntax.domain.account.dto.core.CoreTransactionItemRes;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.report.dto.CategoryRes;
import dev.syntax.domain.report.dto.CoreTransactionRes;
import dev.syntax.domain.report.dto.ReportRes;
import dev.syntax.domain.report.entity.DetailReport;
import dev.syntax.domain.report.entity.SummaryReport;
import dev.syntax.domain.report.enums.Category;
import dev.syntax.domain.report.repository.DetailReportRepository;
import dev.syntax.domain.report.repository.SummaryReportRepository;
import dev.syntax.domain.report.utils.ReportUtils;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import dev.syntax.global.service.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final SummaryReportRepository summaryReportRepository;
    private final DetailReportRepository detailReportRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CoreAccountClient coreAccountClient;

    @Override
    public ReportRes getMonthlyReport(Long userId, int year, int month, UserContext ctx) {
        return getOrGenerateReport(userId, year, month, ctx);
    }



    private void validateAccess(Long childId, UserContext ctx) {
        if (ctx.getId().equals(childId)) return;
        if (ctx.getRole().equals(Role.PARENT.name()) &&
                ctx.getChildren().contains(childId)) return;

        throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
    }

    private BigDecimal fetchPrevTotal(Long userId, int year, int month) {
        int prevYear = year;
        int prevMonth = month - 1;
        if (prevMonth == 0) {
            prevYear -= 1;
            prevMonth = 12;
        }

        // 1. DB에서 조회
        Optional<SummaryReport> report = summaryReportRepository.findByUserAndYearAndMonth(
                userRepository.getReferenceById(userId), prevYear, prevMonth);
        
        if (report.isPresent()) {
            return report.get().getTotalExpense();
        }

        // 2. DB에 없으면 Core API에서 직접 조회하여 계산
        try {
            log.info("[리포트 생성] 전월 리포트 없음 - Core API 직접 조회 시도 ({}년 {}월)", prevYear, prevMonth);
            return calculateActualTotalExpense(userId, prevYear, prevMonth);
        } catch (Exception e) {
            log.warn("[리포트 생성] 전월 데이터 조회 실패: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateActualTotalExpense(Long userId, int year, int month) {
        Account account = accountRepository
                .findByUserIdAndType(userId, AccountType.ALLOWANCE)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        CoreTransactionHistoryRes coreRes = coreAccountClient.getAccountTransactionsByPeriod(
                account.getAccountNo(), startDate, endDate);

        if (coreRes == null || coreRes.transactions() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CoreTransactionItemRes item : coreRes.transactions()) {
            if ("WITHDRAW".equals(item.code())) {
                total = total.add(item.amount());
            }
        }
        
        log.info("[리포트 생성] 전월({}년 {}월) 실제 지출 합계 계산 완료: {}", year, month, total);
        return total;
    }

    @Override
    public void deleteOldReports(int year, int month) {
        // 1년 전 년/월 계산
        int limitYear = year - 1;
        int limitMonth = month;

        List<SummaryReport> oldReports = summaryReportRepository.findAllOldReports(limitYear, limitMonth);
        
        if (!oldReports.isEmpty()) {
            log.info("[리포트 자동 삭제] {}년 {}월 이전 리포트 {}건 삭제 시작", limitYear, limitMonth, oldReports.size());
            
            for (SummaryReport report : oldReports) {
                // 1. DetailReport 삭제
                detailReportRepository.deleteByReport(report);
                // 2. SummaryReport 삭제
                summaryReportRepository.delete(report);
            }
            
            log.info("[리포트 자동 삭제] 완료 - {}건 삭제됨", oldReports.size());
        } else {
            log.info("[리포트 자동 삭제] 삭제할 리포트 없음 (기준: {}년 {}월 이전)", limitYear, limitMonth);
        }
    }



    /**
     * 리포트 조회 또는 생성 (공통 로직)
     */
    private ReportRes getOrGenerateReport(Long userId, int year, int month, UserContext ctx) {

        log.info("========== [리포트 조회/생성 시작] userId: {}, year: {}, month: {} ==========", userId, year, month);

        // 1. 권한 검증
        validateAccess(userId, ctx);

        // 0. 요청 시점 검증: 미래 or 최근 1년 이전 데이터 요청 시 거부
        validateReportTimeRange(year, month);

        // 2. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        // 3. 기존 리포트 확인
        Optional<SummaryReport> existingReport = 
                summaryReportRepository.findByUserAndYearAndMonth(user, year, month);

        if (existingReport.isPresent()) {
            log.info("[리포트 생성] 이미 존재하는 리포트 - reportId: {}", existingReport.get().getId());
            SummaryReport summary = existingReport.get();
            List<DetailReport> details = detailReportRepository.findByReport(summary);
            return buildResponse(summary, details);
        }

        // 4. 새 리포트 생성 - 용돈 계좌 조회
        log.info("[리포트 생성] 새 리포트 생성 시작");
        Account account = accountRepository
                .findByUserIdAndType(userId, AccountType.ALLOWANCE)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        log.info("[리포트 생성] 용돈 계좌 발견 - accountNo: {}", account.getAccountNo());

        // 5. 거래내역 조회
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        log.info("[리포트 생성] 거래내역 조회 - 계좌: {}, 기간: {} ~ {}", account.getAccountNo(), startDate, endDate);
        CoreTransactionHistoryRes coreRes = coreAccountClient.getAccountTransactionsByPeriod(
                account.getAccountNo(), startDate, endDate);

        if (coreRes == null) {
            log.warn("[리포트 생성] 거래내역 응답 null");
            throw new BusinessException(ErrorBaseCode.CORE_API_ERROR);
        }

        // transactions가 null이면 빈 리스트로 처리
        List<CoreTransactionItemRes> transactions = coreRes.transactions() != null 
                ? coreRes.transactions() 
                : List.of();

        log.info("[리포트 생성] 거래내역 조회 완료 - 총 {}건", transactions.size());

        // 6. 지출 거래만 필터링 및 카테고리별 분류
        List<CoreTransactionRes> histories = new ArrayList<>();
        int expenseCount = 0;

        for (CoreTransactionItemRes item : transactions) {
            log.info("[리포트 생성] 거래 확인 - transactionId: {}, merchant: {}, code: {}, category: {}, amount: {}",
                    item.transactionId(), item.merchantName(), item.code(), item.category(), item.amount());
            
            // 지출만 포함 (code가 WITHDRAW인 경우)
            if (!"WITHDRAW".equals(item.code())) {
                log.info("[리포트 생성] 지출 아님 (code: {}) - 건너뜀", item.code());
                continue;
            }
            expenseCount++;

            log.info("[리포트 생성] 지출 거래 추가 - transactionId: {}, category: {}, amount: {}",
                    item.transactionId(), item.category(), item.amount());

            Category category = item.category();
            if (category == null) {
                log.warn("[리포트 생성] 카테고리 정보 없음 - transactionId: {}, merchant: {} -> ETC로 처리",
                        item.transactionId(), item.merchantName());
                category = Category.ETC;
            }

            histories.add(new CoreTransactionRes(category, item.amount()));
        }

        log.info("[리포트 생성] 지출 거래 {}건 추출 완료", expenseCount);

        // 7. 지출이 없는 경우 빈 리포트 생성
        if (histories.isEmpty()) {
            SummaryReport emptyReport = SummaryReport.builder()
                    .user(user)
                    .year(year)
                    .month(month)
                    .totalExpense(BigDecimal.ZERO)
                    .prevTotalExpense(fetchPrevTotal(userId, year, month))
                    .build();

            log.info("[리포트 생성] 빈 요약 리포트 생성 - userId: {}, year: {}, month: {}", userId, year, month);
            summaryReportRepository.save(emptyReport);
            log.info("[리포트 생성] 빈 요약 리포트 저장 완료 - reportId: {}", emptyReport.getId());
            return buildResponse(emptyReport, List.of());
        }

        // 8. 카테고리별 금액 합산
        Map<Category, BigDecimal> amountByCategory = ReportUtils.sumByCategory(histories);
        log.info("[리포트 생성] 카테고리별 합계 계산 완료: {}건", amountByCategory.size());
        amountByCategory.forEach((k, v) -> log.info("  - {}: {}", k, v));

        BigDecimal totalExpense = amountByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 9. 요약 리포트 생성 및 저장
        SummaryReport summary = SummaryReport.builder()
                .user(user)
                .year(year)
                .month(month)
                .totalExpense(totalExpense)
                .prevTotalExpense(fetchPrevTotal(userId, year, month))
                .build();

        log.info("[리포트 생성] 요약 리포트 생성 - userId: {}, year: {}, month: {}, totalExpense: {}",
                userId, year, month, totalExpense);
        summaryReportRepository.save(summary);
        log.info("[리포트 생성] 요약 리포트 저장 완료 - reportId: {}", summary.getId());

        // 10. 상세 리포트 생성 - 상위 3개 카테고리 + 나머지는 ETC로 묶기
        List<DetailReport> details = new ArrayList<>();
        amountByCategory.forEach((category, amount) -> {
            BigDecimal percent = ReportUtils.calcPercent(amount, totalExpense);
            details.add(
                    DetailReport.builder()
                            .report(summary)
                            .category(category)
                            .amount(amount)
                            .percent(percent)
                            .build()
            );
        });

        log.info("[리포트 생성] 상세 리포트 {}건 생성 완료", details.size());
        detailReportRepository.saveAll(details);
        log.info("[리포트 생성] 상세 리포트 저장 완료 - {}건", details.size());

        return buildResponse(summary, details);
    }

    /**
     * 리포트 요청 년/월이 유효한지 검증
     * - 미래 요청 ❌
     * - 현재월 요청 ❌
     * - 최근 1년 이전 요청 ❌
     */
    private void validateReportTimeRange(int year, int month) {

        LocalDate now = LocalDate.now();
        LocalDate nowMonth = now.withDayOfMonth(1);
        LocalDate requested = LocalDate.of(year, month, 1);

        // 1️⃣ 미래 조회 불가
        if (requested.isAfter(nowMonth)) {
            throw new BusinessException(ErrorBaseCode.REPORT_OUT_OF_RANGE);
        }

        // 2️⃣ 현재월 조회 불가
        if (requested.isEqual(nowMonth)) {
            throw new BusinessException(ErrorBaseCode.REPORT_NOT_AVAILABLE_YET);
        }

        // 3️⃣ 최근 1년 초과 요청 불가
        LocalDate oneYearBefore = now.minusYears(1).withDayOfMonth(1);
        if (requested.isBefore(oneYearBefore)) {
            log.warn("[리포트 조회] 최근 1년 이외 요청: {}년 {}월 (허용: {} ~ {})",
                    year, month, oneYearBefore, nowMonth.minusMonths(1));
            throw new BusinessException(ErrorBaseCode.REPORT_OUT_OF_RANGE);
        }
    }






    private ReportRes buildResponse(SummaryReport summary, List<DetailReport> details) {
        BigDecimal prev = summary.getPrevTotalExpense() == null
                ? BigDecimal.ZERO
                : summary.getPrevTotalExpense();

        BigDecimal diff = summary.getTotalExpense().subtract(prev).abs();

        String comparedType;
        int comparison = summary.getTotalExpense().compareTo(prev);
        if (comparison > 0) comparedType = "more";
        else if (comparison < 0) comparedType = "less";
        else comparedType = "same";

        // 1. 퍼센트 기준 내림차순 정렬
        List<DetailReport> sortedDetails = details.stream()
                .sorted((d1, d2) -> d2.getAmount().compareTo(d1.getAmount()))
                .toList();

        List<CategoryRes> categoryList = new ArrayList<>();

        // 2. 4개 이하인 경우 그대로 반환
        if (sortedDetails.size() <= 4) {
            categoryList = sortedDetails.stream()
                    .map(d -> new CategoryRes(
                            d.getCategory().getKoreanName(),
                            Utils.NumberFormattingService(d.getAmount()),
                            d.getPercent().doubleValue()
                    ))
                    .toList();
        } else {
            // 3. 4개 초과인 경우: 상위 3개 + 나머지(기타)
            // 상위 3개 추가
            for (int i = 0; i < 3; i++) {
                DetailReport d = sortedDetails.get(i);
                categoryList.add(new CategoryRes(
                        d.getCategory().getKoreanName(),
                        Utils.NumberFormattingService(d.getAmount()),
                        d.getPercent().doubleValue()
                ));
            }

            // 나머지 합산
            BigDecimal etcAmount = BigDecimal.ZERO;
            BigDecimal etcPercent = BigDecimal.ZERO;

            for (int i = 3; i < sortedDetails.size(); i++) {
                DetailReport d = sortedDetails.get(i);
                etcAmount = etcAmount.add(d.getAmount());
                etcPercent = etcPercent.add(d.getPercent());
            }

            // 기타 항목 추가
            categoryList.add(new CategoryRes(
                    "기타",
                    Utils.NumberFormattingService(etcAmount),
                    etcPercent.doubleValue()
            ));
        }

        return new ReportRes(
                summary.getMonth(),
                Utils.NumberFormattingService(summary.getTotalExpense()),
                Utils.NumberFormattingService(diff),
                comparedType,
                categoryList
        );
    }
}

