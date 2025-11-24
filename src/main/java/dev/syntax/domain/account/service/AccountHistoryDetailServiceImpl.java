package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.AccountHistoryDetailRes;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountHistoryDetailServiceImpl implements AccountHistoryDetailService {

    private final AccountRepository accountRepository;

    @Override
    public AccountHistoryDetailRes getDetail(Long transactionId, UserContext ctx) {

        log.info("거래 상세 조회 요청 transactionId={}, userId={}", transactionId, ctx.getId());

        // 1️⃣ 거래ID 유효성 검증
        if (transactionId == null || transactionId <= 0) {
            throw new BusinessException(ErrorBaseCode.TX_INVALID_TRANSACTION_ID);
        }

        // 2️⃣ Mock 데이터 조회
        AccountHistoryDetailRes detail = mockCoreDetail(transactionId);

        if (detail == null) {
            throw new BusinessException(ErrorBaseCode.TX_NOT_FOUND);
        }

        // 3️⃣ 거래아이디 → 실제 계좌No 추출 규칙(예시)
        // ex: 202501150001 → 앞 4~10 자리 = 계좌번호 일부
        // 실서비스에서는 transaction에 accountId가 붙어있음
        Long mockAccountId = extractAccountIdFromMock(transactionId);

        // 4️⃣ 사용자의 계좌인지 체크
        validateOwnership(mockAccountId, ctx);

        return detail;
    }

    /**
     * Mock 규칙: 거래아이디 일부에서 계좌ID 추출 (실서비스에서는 불필요)
     * 나중에 삭제 예정
     */
    private Long extractAccountIdFromMock(Long transactionId) {
        // 예시: 맨 마지막 3자리만 계좌ID라고 가정
        return transactionId % 1000; 
    }

    /**
     * 사용자가 해당 계좌를 조회할 권한이 있는지 검증
     */
    private void validateOwnership(Long accountId, UserContext ctx) {

        // 계좌 존재하는지 확인
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.TX_ACCOUNT_NOT_FOUND));

        Long ownerId = account.getUser().getId(); // 계좌 소유자

        // 자녀일 경우
        if (ctx.getRole().equals(Role.CHILD.name())) {
            if (!ctx.getId().equals(ownerId)) {
                throw new BusinessException(ErrorBaseCode.TX_NO_PERMISSION);
            }
            return;
        }

        // 부모일 경우 (본인 or 자녀의 계좌이면 OK)
        if (!ctx.getId().equals(ownerId) && !ctx.getChildren().contains(ownerId)) {
            throw new BusinessException(ErrorBaseCode.TX_NO_PERMISSION);
        }
    }

    /**
     * Mock 데이터 리턴
     */
    private AccountHistoryDetailRes mockCoreDetail(Long transactionId) {

        if (transactionId.equals(202501150001L)) {
            return new AccountHistoryDetailRes(
                    "이체", "50,000", "2025-01-15 13:22",
                    "일시불", "이체", "50,000", "150,000"
            );
        }

        if (transactionId.equals(202501150002L)) {
            return new AccountHistoryDetailRes(
                    "편의점", "1,500", "2025-01-15 14:10",
                    "일시불", "식비", "1,500", "148,500"
            );
        }

        if (transactionId.equals(202501160001L)) {
            return new AccountHistoryDetailRes(
                    "스타벅스", "5,300", "2025-01-16 10:23",
                    "할부", "카페/간식", "5,300", "143,200"
            );
        }

        return null;
    }
}
