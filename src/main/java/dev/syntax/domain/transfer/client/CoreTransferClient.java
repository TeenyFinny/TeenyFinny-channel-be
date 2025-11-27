package dev.syntax.domain.transfer.client;

import dev.syntax.global.core.CoreApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CoreTransferClient {

    private final RestTemplate coreRestTemplate;
    private final CoreApiProperties properties;

    private final String DELETE_AUTO_TRANSFER_URL = "/core/banking/auto-transfer/{autoTransferId}";

    public void deleteAutoTransfer(Long autoTransferId) {
        coreRestTemplate.delete(
                properties.getBaseUrl() + DELETE_AUTO_TRANSFER_URL,
                autoTransferId
        );
    }
}
