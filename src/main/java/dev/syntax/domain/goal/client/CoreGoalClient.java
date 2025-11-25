package dev.syntax.domain.goal.client;

import dev.syntax.domain.goal.dto.CoreTransactionHistoryRes;
import dev.syntax.global.core.CoreApiProperties;
import dev.syntax.global.core.CoreResponseWrapper;
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
        CoreResponseWrapper<CoreTransactionHistoryRes> wrapper =
                coreRestTemplate.exchange(
                        properties.getBaseUrl() + GET_HISTORY_URL + accountNo,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<CoreResponseWrapper<CoreTransactionHistoryRes>>() {}
                ).getBody();

        return wrapper.getData();
    }


}
