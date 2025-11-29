package dev.syntax.domain.account.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreCreateAccountReq;
import dev.syntax.domain.account.dto.core.CoreGoalAccountReq;
import dev.syntax.domain.account.dto.core.CoreInvestmentAccountRes;
import dev.syntax.domain.account.dto.core.CoreTransactionHistoryDetailRes;
import dev.syntax.domain.account.dto.core.CoreTransactionDetailItemRes;
import dev.syntax.domain.account.dto.core.CoreTransactionHistoryRes;
import dev.syntax.domain.account.dto.core.CoreUserAccountListRes;
import dev.syntax.global.core.CoreApiProperties;
import lombok.RequiredArgsConstructor;

/**
 * Core 뱅킹 서버의 계좌 조회 API를 호출하는 클라이언트입니다.
 * <p>
 * 사용자의 전체 계좌 정보를 조회하며, 부모일 경우 자녀의 계좌까지 포함하여 반환합니다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class CoreAccountClient {
	private final RestTemplate coreRestTemplate;
	private final CoreApiProperties properties;

	/** Core 서버의 계좌 조회 API 엔드포인트 */
	private static final String ACCOUNT_URL = "/core/banking/account";
	private static final String INVESTMENT_ACCOUNT_URL = "/core/banking/account/investment";
	private static final String ALLOWANCE_ACCOUNT_URL = "/core/banking/account/create";
	/**
	 * Core 서버에서 사용자의 전체 계좌 정보를 조회합니다.
	 * <p>
	 * 부모 사용자일 경우 자녀의 계좌 정보도 함께 반환됩니다.
	 * </p>
	 *
	 * @return 사용자 계좌 목록 (부모일 경우 자녀 계좌 포함)
	 * @throws dev.syntax.global.exception.CoreApiException Core 서버 API 호출 중 에러 발생 시
	 */
	public CoreUserAccountListRes getUserAccounts() {
		return coreRestTemplate.getForObject(
			properties.getBaseUrl() + ACCOUNT_URL,
			CoreUserAccountListRes.class
		);
	}

	/**
	 * Core 서버에 투자 계좌를 생성합니다.
	 * @param userId 사용자 ID
	 * @return 투자 계좌 정보
	 */
	public CoreInvestmentAccountRes createInvestmentAccount(Long userId) {
		return coreRestTemplate.postForObject(
			properties.getBaseUrl() + INVESTMENT_ACCOUNT_URL + "?userId=" + userId,
			null,
			CoreInvestmentAccountRes.class
		);
	}

	private static final String GOAL_ACCOUNT_URL = "/core/banking/goal/account";

	public CoreAccountItemRes createGoalAccount(CoreGoalAccountReq req) {
		return coreRestTemplate.postForObject(
			properties.getBaseUrl() + GOAL_ACCOUNT_URL,
			req,
			CoreAccountItemRes.class
		);
	}

	/**
	 * Core 서버에 자녀 사용자의 용돈 계좌를 생성합니다.
	 * @param req 자녀 사용자 계좌 개설 요청
	 * @return 계좌 정보
	 */
	public CoreAccountItemRes createChildAccount(CoreCreateAccountReq req) {
		return coreRestTemplate.postForObject(
			properties.getBaseUrl() + ALLOWANCE_ACCOUNT_URL,
			req,
			CoreAccountItemRes.class
		);
	}

	/**
	 * Core 서버에서 특정 계좌의 년/월별 거래내역을 조회합니다.
	 *
	 * @param accountNumber 계좌번호
	 * @param year 조회할 년도
	 * @param month 조회할 월
	 * @return 거래 내역 및 잔액 정보
	 */
	public CoreTransactionHistoryRes getAccountTransactionsByMonth(
			String accountNumber, int year, int month) {

		String url = properties.getBaseUrl() +
				"/core/transaction/account/{accountNo}/{year}/{month}";

		return coreRestTemplate.getForObject(
				url,
				CoreTransactionHistoryRes.class,
				accountNumber,
				year,
				month
		);
	}


	/**
	 * Core 서버에서 특정 거래의 상세 정보를 조회합니다.
	 * <p>
	 * 거래 ID로 단일 거래를 조회하여 거래 타입, 카테고리, 승인 금액 등
	 * 상세 정보를 반환합니다.
	 * </p>
	 *
	 * @param transactionId 조회할 거래 ID
	 * @return 거래 상세 정보
	 */
	public CoreTransactionDetailItemRes getTransactionDetail(Long transactionId) {
		String url = properties.getBaseUrl() +
				"/core/transaction/detail/{transactionId}";

		return coreRestTemplate.getForObject(
				url,
				CoreTransactionDetailItemRes.class,
				transactionId
		);
	}
}
