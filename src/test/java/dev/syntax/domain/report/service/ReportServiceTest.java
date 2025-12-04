package dev.syntax.domain.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.report.dto.CategoryRes;
import dev.syntax.domain.report.dto.ReportRes;
import dev.syntax.domain.report.entity.DetailReport;
import dev.syntax.domain.report.entity.SummaryReport;
import dev.syntax.domain.report.enums.Category;
import dev.syntax.domain.report.repository.DetailReportRepository;
import dev.syntax.domain.report.repository.SummaryReportRepository;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @InjectMocks
    private ReportServiceImpl reportService;

    @Mock
    private SummaryReportRepository summaryReportRepository;

    @Mock
    private DetailReportRepository detailReportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CoreAccountClient coreAccountClient;

    /**
     * TC-ALLOWANCE-006 : 소비 리포트 조회: 월별 수입·지출·카테고리 통계 반환
     * GET /allowance/report
     *
     * <p>테스트 시나리오:</p>
     * <ul>
     *   <li>자녀가 본인의 월별 리포트 조회 요청 (GET /allowance/report)</li>
     *   <li>기존에 생성된 SummaryReport와 DetailReport가 존재하는 경우</li>
     *   <li>ReportService가 저장된 리포트 데이터를 조회하여 반환</li>
     *   <li>반환된 ReportRes의 총 지출, 전월 대비 차액, 카테고리별 통계가 정확한지 검증</li>
     *   <li>카테고리 리스트가 지출 비중(퍼센트) 순으로 정렬되어 있는지 검증</li>
     * </ul>
     */
    @Test
    @DisplayName("자녀 본인 월별 리포트 조회 성공 - 기존 리포트 반환")
    void getMonthlyReport_success() {
        // given
        Long userId = 2L;
        int year = 2025;
        int month = 1;

        User user = User.builder()
                .id(userId)
                .role(Role.CHILD)
                .build();

        UserContext ctx = new UserContext(user);

        SummaryReport summary = SummaryReport.builder()
                .id(100L)
                .user(user)
                .year(year)
                .month(month)
                .totalExpense(new BigDecimal("50000"))
                .prevTotalExpense(new BigDecimal("30000"))
                .build();

        DetailReport detail1 = DetailReport.builder()
                .category(Category.FOOD)
                .amount(new BigDecimal("20000"))
                .percent(new BigDecimal("40"))
                .build();

        DetailReport detail2 = DetailReport.builder()
                .category(Category.TRANSPORT)
                .amount(new BigDecimal("30000"))
                .percent(new BigDecimal("60"))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(summaryReportRepository.findByUserAndYearAndMonth(user, year, month))
                .thenReturn(Optional.of(summary));
        when(detailReportRepository.findByReport(summary))
                .thenReturn(List.of(detail1, detail2));

        // when
        ReportRes res = reportService.getMonthlyReport(userId, year, month, ctx);

        // then
        assertThat(res.getMonth()).isEqualTo(month);
        assertThat(res.getTotalAmount()).isEqualTo("50,000"); // format 적용됨
        assertThat(res.getComparedAmount()).isEqualTo("20,000");         // 50k - 30k = 20k
        assertThat(res.getComparedType()).isEqualTo("more");   // 이번달 > 지난달

        assertThat(res.getCategories()).hasSize(2);
        CategoryRes firstCategory = res.getCategories().get(0);

        assertThat(firstCategory.getCategory()).isEqualTo("교통"); // 퍼센트가 더 큰 카테고리가 앞에 와야 함!
        assertThat(firstCategory.getAmount()).isEqualTo("30,000");
        assertThat(firstCategory.getPercentage()).isEqualTo(60.0);

        verify(summaryReportRepository).findByUserAndYearAndMonth(user, year, month);
        verify(detailReportRepository).findByReport(summary);
    }
    
}
