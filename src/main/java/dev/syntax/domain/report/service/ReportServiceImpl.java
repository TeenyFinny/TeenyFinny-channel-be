package dev.syntax.domain.report.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreTransactionHistoryRes;
import dev.syntax.domain.account.dto.core.CoreTransactionItemRes;
import dev.syntax.domain.account.dto.core.CoreUserAccountListRes;
import dev.syntax.domain.account.enums.AccountType;
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
    private final CoreAccountClient coreAccountClient;

    @Override
    public ReportRes getMonthlyReport(Long userId, int month, UserContext ctx) {

        LocalDate now = LocalDate.now();
        java.time.YearMonth requestedYm = java.time.YearMonth.of(2025, month);
        java.time.YearMonth currentYm = java.time.YearMonth.from(now);
        if (!requestedYm.isBefore(currentYm)) {
            throw new BusinessException(ErrorBaseCode.REPORT_NOT_AVAILABLE_YET);
        }

        validateAccess(userId, ctx);

        User childUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

        Optional<SummaryReport> exist =
                summaryReportRepository.findByUserIdAndMonth(userId, month);

        if (exist.isPresent()) {
            SummaryReport summary = exist.get();
            List<DetailReport> details =
                    detailReportRepository.findByReportId(summary.getId());
            return buildResponse(summary, details);
        }

        log.info("[리포트 생성] user={}, month={}, Core 서버 조회 시작", userId, month);

        CoreUserAccountListRes userAccounts = coreAccountClient.getUserAccounts();
        List<CoreAccountItemRes> targetAccounts = findTargetUserAccounts(userAccounts, userId);

        List<CoreTransactionRes> histories = new ArrayList<>();
        int currentYear = 2025;

        for (CoreAccountItemRes acc : targetAccounts) {
            if (acc.accountType() != AccountType.ALLOWANCE) continue;

            log.info("ALW 계좌 감지: {}", acc.accountNumber());

            // 2. 거래내역 조회
            LocalDate startDate = LocalDate.of(currentYear, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            CoreTransactionHistoryRes txRes = coreAccountClient.getAccountTransactionsByPeriod(
                    acc.accountNumber(), startDate, endDate);

            if (txRes != null && txRes.transactions() != null) {
                log.info("거래 {}건 조회됨", txRes.transactions().size());

                int expenseCount = 0;

                for (CoreTransactionItemRes item : txRes.transactions()) {
                    // 지출만 포함
                    if (item.amount().compareTo(BigDecimal.ZERO) >= 0) {
                        continue;
                    }
                    expenseCount++;

                    // 이제 items에 category 정보가 포함되어 있음
                    log.debug("[리포트 생성] 거래 처리 - merchant: {}, category: {}",
                            item.merchantName(), item.category());

                    Category category = item.category();
                    if (category == null) {
                         log.warn("[리포트 생성] 카테고리 정보 없음 - merchant: {} -> ETC로 처리",
                                 item.merchantName());
                         category = Category.ETC;
                    }

                    histories.add(new CoreTransactionRes(category, item.amount().abs()));
                }

                log.info("지출 {}건 반영", expenseCount);
            }
        }

        if (histories.isEmpty()) {
            SummaryReport emptyReport = SummaryReport.builder()
                    .user(childUser)
                    .month(month)
                    .totalExpense(BigDecimal.ZERO)
                    .prevTotalExpense(fetchPrevTotal(userId, month))
                    .build();

            summaryReportRepository.save(emptyReport);
            return buildResponse(emptyReport, List.of());
        }

        Map<Category, BigDecimal> amountByCategory = ReportUtils.sumByCategory(histories);
        BigDecimal totalExpense = amountByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SummaryReport summary = SummaryReport.builder()
                .user(childUser)
                .month(month)
                .totalExpense(totalExpense)
                .prevTotalExpense(fetchPrevTotal(childUser.getId(), month))
                .build();
        summaryReportRepository.save(summary);

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
        detailReportRepository.saveAll(details);

        return buildResponse(summary, details);
    }

    private List<CoreAccountItemRes> findTargetUserAccounts(CoreUserAccountListRes res, Long targetUserId) {
        if (res.children() != null) {
            for (var childAcc : res.children()) {
                if (childAcc.userId().equals(targetUserId)) {
                    return childAcc.accounts();
                }
            }
        }
        return res.accounts();
    }

    private void validateAccess(Long childId, UserContext ctx) {
        if (ctx.getId().equals(childId)) return;
        if (ctx.getRole().equals(Role.PARENT.name()) &&
                ctx.getChildren().contains(childId)) return;

        throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
    }

    private BigDecimal fetchPrevTotal(Long userId, int month) {
        if (month == 1) return BigDecimal.ZERO;

        return summaryReportRepository.findByUserIdAndMonth(userId, month - 1)
                .map(SummaryReport::getTotalExpense)
                .orElse(BigDecimal.ZERO);
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

        List<CategoryRes> categoryList = details.stream()
                .map(d -> new CategoryRes(
                        d.getCategory().getKoreanName(),
                        Utils.NumberFormattingService(d.getAmount()),
                        d.getPercent().doubleValue()
                ))
                .toList();

        return new ReportRes(
                summary.getMonth(),
                Utils.NumberFormattingService(summary.getTotalExpense()),
                Utils.NumberFormattingService(diff),
                comparedType,
                categoryList
        );
    }
}
