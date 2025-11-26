package dev.syntax.domain.report.service;

import dev.syntax.domain.report.dto.CategoryRes;
import dev.syntax.domain.report.dto.CoreTransactionRes;
import dev.syntax.domain.report.dto.ReportRes;
import dev.syntax.domain.report.entity.DetailReport;
import dev.syntax.domain.report.entity.SummaryReport;
import dev.syntax.domain.report.enums.Category;
import dev.syntax.domain.report.enums.CategoryMapper;
import dev.syntax.domain.report.mock.MockCoreBankClient;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final SummaryReportRepository summaryReportRepository;
    private final DetailReportRepository detailReportRepository;
    private final UserRepository userRepository;
    private final MockCoreBankClient coreBankClient;

    @Override
    public ReportRes getMonthlyReport(Long userId, int month, UserContext ctx) {
        // 조회 기간 검증 (현재 월 포함 미래는 조회 불가)
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        // 연도까지 고려해야 하지만, 요구사항상 month만 입력받으므로 일단 단순 비교
        // (실제 서비스라면 연도도 입력받아야 함. 여기서는 현재 연도 기준 가정 or 단순 month 비교)
        // 요구사항: "현재 월에 접근하려고 하면 에러"
        if (month >= currentMonth) {
            throw new BusinessException(ErrorBaseCode.REPORT_NOT_AVAILABLE_YET);
        }
        // 0. 검증
        validateAccess(userId, ctx);

        User childUser = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));        

        // 1. summary_report 에 저장된 리포트 먼저 조회
        Optional<SummaryReport> exist = 
                summaryReportRepository.findByUserIdAndMonth(userId, month);

        if (exist.isPresent()) {
            SummaryReport summary = exist.get();
            List<DetailReport> details =
                    detailReportRepository.findByReportId(summary.getId());

            return buildResponse(summary, details);
        }

        // 2. 없으면 CoreBank 서버에서 해당 월 거래내역 조회
        List<CoreTransactionRes> histories =
                coreBankClient.getMonthlyHistory(userId, month);

        // 3. 거래내역이 없는 경우 → 빈 리포트 생성 후 반환
        if (histories.isEmpty()) {

            SummaryReport emptyReport = SummaryReport.builder()
                    .user(childUser)
                    .month(month)
                    .totalExpense(BigDecimal.ZERO)
                    .prevTotalExpense(fetchPrevTotal(userId, month))
                    .build();

            summaryReportRepository.save(emptyReport);

            return buildResponse(emptyReport, List.of()); // categories = []
        }

        // 4. 거래내역이 있는 경우 → 카테고리별 금액 합산
        Map<Category, BigDecimal> amountByCategory = ReportUtils.sumByCategory(histories);
        BigDecimal totalExpense = amountByCategory.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. 요약 리포트 저장
        SummaryReport summary = SummaryReport.builder()
                .user(childUser)
                .month(month)
                .totalExpense(totalExpense)
                .prevTotalExpense(fetchPrevTotal(childUser.getId(), month))
                .build();

        summaryReportRepository.save(summary);

        // 6. DetailReport 저장
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

        // 7. 최종 응답 생성
        return buildResponse(summary, details);
    }

    private void validateAccess(Long childId, UserContext ctx) {
        // 자녀 본인의 리포트 조회 ok
        if (ctx.getId().equals(childId)) {
            return;
        }

        // 부모가 자신의 자녀 리포트 조회 ok
        if (ctx.getRole().equals(Role.PARENT.name()) &&
                ctx.getChildren().contains(childId)) {
            return; 
        }

        // 그 외는 조회 불가
        throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
    }

    /**
     * 전월 소비 총액 조회
     */
    private BigDecimal fetchPrevTotal(Long userId, int month) {
        int prev = (month == 1) ? 12 : month - 1;

        return summaryReportRepository.findByUserIdAndMonth(userId, prev)
                .map(SummaryReport::getTotalExpense)
                .orElse(BigDecimal.ZERO);
    }


    /**
     * SummaryReport + DetailReport -> Front Response 변환
     */
    private ReportRes buildResponse(SummaryReport summary, List<DetailReport> details) {

        BigDecimal prev = summary.getPrevTotalExpense() == null
                ? BigDecimal.ZERO
                : summary.getPrevTotalExpense();

        BigDecimal diff = summary.getTotalExpense().subtract(prev).abs();

        String comparedType =
                summary.getTotalExpense().compareTo(prev) >= 0 ? "more" : "less";

        List<CategoryRes> categoryList = details.stream()
                .map(d -> new CategoryRes(
                        CategoryMapper.toKorean(d.getCategory()), // Enum -> 한글 변환
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
