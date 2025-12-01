package dev.syntax.domain.goal.client;

import dev.syntax.domain.goal.dto.*;
import dev.syntax.global.core.CoreApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CoreGoalClient {
    private final RestTemplate coreRestTemplate;
    private final CoreApiProperties properties;

    private final String GET_HISTORY_URL = "/core/transaction/account/";
    private final String UPDATE_ACCOUNT_STATUS_URL = "/core/banking/account/{accountNo}/status";
    private final String UPDATE_PAY_DAY_URL = "/core/banking/auto-transfer/{autoTransferId}/pay-day";

    public CoreTransactionHistoryRes getAccountHistory(String accountNo) {
        return coreRestTemplate.getForObject(
                properties.getBaseUrl() + GET_HISTORY_URL + "{accountNo}",
                CoreTransactionHistoryRes.class,
                accountNo
        );
    }

    public CoreUpdateAccountStatusRes updateAccountStatus(String accountNo, CoreUpdateAccountStatusReq req) {

        return coreRestTemplate.exchange(
                properties.getBaseUrl() + UPDATE_ACCOUNT_STATUS_URL,
                HttpMethod.PUT,
                new HttpEntity<>(req),
                CoreUpdateAccountStatusRes.class,
                accountNo
        ).getBody();
    }

    public CoreUpdateAutoTransferDayRes updateAutoTransferDay(Long autoTransferId, Integer payDay) {

        return coreRestTemplate.exchange(
                properties.getBaseUrl() + UPDATE_PAY_DAY_URL,
                HttpMethod.PUT,
                new HttpEntity<>(payDay),
                CoreUpdateAutoTransferDayRes.class,
                autoTransferId
        ).getBody();
    }
}


