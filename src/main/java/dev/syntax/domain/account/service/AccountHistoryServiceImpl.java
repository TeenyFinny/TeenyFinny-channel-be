package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountHistoryReq;
import dev.syntax.domain.account.dto.AccountHistoryRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 계좌 거래내역 조회 서비스 구현체.
 *
 * UserContext를 기반으로 부모/자녀 권한을 체크하고,
 * 코어 뱅킹 서버(CoreBankClient)에서 거래내역을 조회하여
 * 프론트가 요구하는 형태(AccountHistoryRes)로 변환한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountHistoryServiceImpl implements AccountHistoryService {

    private final AccountRepository accountRepository;

    @Override
    public List<AccountHistoryRes> getHistory(Long userId, AccountHistoryReq req, UserContext ctx) {
        // 1. 부모/자녀 인가 체크
        validateUserAccess(userId, ctx);

        // 2. 계좌 존재 확인
        Account account = accountRepository
                .findByUserIdAndType(userId, req.accountType())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.NOT_FOUND_ENTITY));

        // 3. 조회 기간 계산
        LocalDateTime start = LocalDate.of(req.year(), req.month(), 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(req.year(), req.month(), 1)
                .withDayOfMonth(LocalDate.of(req.year(), req.month(), 1).lengthOfMonth())
                .atTime(LocalTime.MAX);

        log.info("거래내역 조회 → 계좌번호: {}, 기간: {} ~ {}",
                account.getAccountNo(), start, end);

        // 4. Core 서버 Mock 호출
        List<AccountHistoryRes> mock = mockCoreHistory(account.getAccountNo(), start, end);

        return mock;
        // core 서버 호출
        // 계좌번호, req.month, year 20201001, 20201031 전달해줘야됨
        // 코어에서 조회를 통해 거래구분, 거래 아이디, 거래후 잔액, 거래처명, 카테고리, 거래일자, 거래금액을 가져옴
        // 호출받은 데이터를 가공해서 리턴
    }

    private void validateUserAccess(Long targetUserId, UserContext ctx) {

        if (ctx.getRole().equals(Role.CHILD)) {
            if (!ctx.getId().equals(targetUserId)) {
                throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
            }
            return;
        }

        // [수정] 부모가 본인을 조회하거나, 자녀를 조회하는 경우 허용
        if (ctx.getId().equals(targetUserId)) {
            return;
        }

        if (!ctx.getChildren().contains(targetUserId)) {
            throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
        }
    }

    /**
     * 코어 서버 mock 응답.
     * 실제 구현에서는 RestTemplate/WebClient로 코어 호출.
     */
    private List<AccountHistoryRes> mockCoreHistory(String accountNo,
            LocalDateTime start,
            LocalDateTime end) {

        return List.of(
                new AccountHistoryRes(
                        "T202501150001",
                        "deposit",
                        "이체",
                        "50,000",
                        "150,000",
                        "2025-01-15 13:22"),
                new AccountHistoryRes(
                        "T202501150002",
                        "withdrawal",
                        "편의점",
                        "1,500",
                        "148,500",
                        "2025-01-15 14:10"),
                new AccountHistoryRes(
                        "T202501160001",
                        "withdrawal",
                        "스타벅스",
                        "5,300",
                        "143,200",
                        "2025-01-16 10:23"));
    }
}