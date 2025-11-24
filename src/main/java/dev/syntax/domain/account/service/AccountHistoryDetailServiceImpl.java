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

/**
 * <h2>거래 단일 상세 조회 서비스 구현체</h2>
 *
 * Core 서버(은행 시스템)로부터 특정 거래의 상세 정보를 조회하는 서비스입니다.
 * 현재는 코어 연동 전 단계이므로 Mock 데이터를 기반으로 동작하며,
 * 인증/인가 검증 로직은 실제 서비스와 동일하게 유지됩니다.
 *
 * <p><b>주요 기능:</b></p>
 * <ul>
 *     <li>거래 ID 유효성 검증</li>
 *     <li>Mock Core 데이터 조회 (향후 Core 연동 시 제거 예정)</li>
 *     <li>거래가 속한 계좌ID 추출 및 조회 권한 검증</li>
 *     <li>자녀/부모 권한별 접근 제한 처리</li>
 * </ul>
 *
 * <p>
 * ⚠️ <b>주의:</b> 현재는 Core API 연동 전이므로 거래 엔티티가 DB에 존재하지 않고,  
 * transactionId → accountId 매핑도 Mock 규칙을 사용합니다.
 * 실제 Core 연동 시 <code>Transaction</code> 엔티티에서 직접 accountId를 조회하도록 변경됩니다.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountHistoryDetailServiceImpl implements AccountHistoryDetailService {

    private final AccountRepository accountRepository;

    /**
     * <h3>📌 단일 거래 상세 조회</h3>
     *
     * 주어진 거래 ID에 해당하는 상세 정보를 조회하고,
     * 현재 로그인한 사용자(UserContext)가 접근 가능한 거래인지 권한을 검증합니다.
     *
     * <p><b>검증 절차:</b></p>
     * <ol>
     *     <li>거래 ID가 Null 또는 0 이하인지 유효성 검증</li>
     *     <li>Mock 데이터에서 거래 상세 정보 조회</li>
     *     <li>Mock 규칙에 따라 해당 거래가 속한 계좌 ID 추출</li>
     *     <li>계좌의 실제 소유자 정보 조회</li>
     *     <li>자녀/부모 권한에 따라 접근 가능한지 검증</li>
     * </ol>
     *
     * <p><b>예외:</b></p>
     * <ul>
     *     <li>{@link ErrorBaseCode#TX_INVALID_TRANSACTION_ID} - 잘못된 거래 ID</li>
     *     <li>{@link ErrorBaseCode#TX_NOT_FOUND} - Mock 데이터에 존재하지 않는 거래</li>
     *     <li>{@link ErrorBaseCode#TX_ACCOUNT_NOT_FOUND} - 해당 계좌 없음</li>
     *     <li>{@link ErrorBaseCode#TX_NO_PERMISSION} - 접근 권한 없음</li>
     * </ul>
     *
     * @param transactionId 조회할 거래 ID
     * @param ctx           로그인한 사용자 컨텍스트
     * @return 조회된 단일 거래 상세 정보
     */
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

        // 3️⃣ 거래ID → 계좌ID Mock 규칙으로 추출
        Long mockAccountId = extractAccountIdFromMock(transactionId);

        // 4️⃣ 사용자의 조회 권한 검증
        validateOwnership(mockAccountId, ctx);

        return detail;
    }

    /**
     * <h3>📌 Mock 규칙을 통해 계좌 ID 추출</h3>
     *
     * Core 연동 전까지는 거래 ID 일부를 사용해 계좌 ID를 흉내냅니다.
     * <p>
     * 실제 Core 연동 시 Transaction 엔티티에서 직접 accountId를 조회하므로 본 메서드는 삭제됩니다.
     * </p>
     *
     * @param transactionId 거래 ID
     * @return Mock으로 생성된 계좌 ID
     */
    private Long extractAccountIdFromMock(Long transactionId) {
        // 예시: 맨 마지막 3자리만 계좌ID라고 가정
        return transactionId % 1000;
    }

    /**
     * <h3>📌 해당 계좌를 조회할 권한이 있는지 검증</h3>
     *
     * <ul>
     *     <li>자녀(CHILD): 본인 계좌만 조회 가능</li>
     *     <li>부모(PARENT): 자신의 계좌 + 연결된 자녀의 계좌 조회 가능</li>
     * </ul>
     *
     * @param accountId 실제 조회할 계좌 ID
     * @param ctx       로그인한 사용자 정보(UserContext)
     */
    private void validateOwnership(Long accountId, UserContext ctx) {

        // 계좌 존재하는지 확인
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.TX_ACCOUNT_NOT_FOUND));

        Long ownerId = account.getUser().getId(); // 계좌 소유자 ID

        // 자녀일 경우: 본인 계좌만 가능
        if (ctx.getRole().equals(Role.CHILD.name())) {
            if (!ctx.getId().equals(ownerId)) {
                throw new BusinessException(ErrorBaseCode.TX_NO_PERMISSION);
            }
            return;
        }

        // 부모일 경우: 본인 + children 목록에 있는 자녀 계좌만 가능
        if (!ctx.getId().equals(ownerId) && !ctx.getChildren().contains(ownerId)) {
            throw new BusinessException(ErrorBaseCode.TX_NO_PERMISSION);
        }
    }

    /**
     * <h3>📌 Mock 데이터 응답</h3>
     *
     * Core API 연동 전, 테스트를 위해 특정 transactionId에 대해
     * 하드코딩된 거래 상세 정보를 반환합니다.
     *
     * <p>
     * 실제 Core 연동 시 삭제됩니다.
     * </p>
     *
     * @param transactionId 거래 ID
     * @return Mock 상세 정보 (없으면 null)
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
