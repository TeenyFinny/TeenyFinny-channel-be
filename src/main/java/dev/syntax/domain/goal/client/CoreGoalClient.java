package dev.syntax.domain.goal.client;

import dev.syntax.domain.goal.dto.CoreTransactionHistoryRes;
import dev.syntax.global.core.CoreApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CoreGoalClient {
    private final RestTemplate coreRestTemplate;
    private final CoreApiProperties properties;

    private final String GET_HISTORY_URL = "/core/transaction/account/";

    public CoreTransactionHistoryRes getAccountHistory(String accountNo) {
        return coreRestTemplate.getForObject(
                properties.getBaseUrl() + GET_HISTORY_URL + accountNo,
                CoreTransactionHistoryRes.class
        );
    }
}
