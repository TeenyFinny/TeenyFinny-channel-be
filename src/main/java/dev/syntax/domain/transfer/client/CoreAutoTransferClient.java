package dev.syntax.domain.transfer.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import dev.syntax.domain.transfer.dto.CoreAutoTransferReq;
import dev.syntax.domain.transfer.dto.CoreAutoTransferRes;
import dev.syntax.global.core.CoreApiProperties;
import lombok.RequiredArgsConstructor;

/**
 * Core 뱅킹 서버의 자동이체 생성 API를 호출하는 클라이언트입니다.
 */
@Service
@RequiredArgsConstructor
public class CoreAutoTransferClient {
    private final RestTemplate coreRestTemplate;
	private final CoreApiProperties properties;

    private static final String AUTO_TRANSFER_CREATE_URL = "/core/banking/account/auto-transfer/create";
    private static final String AUTO_TRANSFER_UPDATE_URL = "/core/banking/account/auto-transfer/{autoTransferId}";

    /**
     * Core 자동이체 생성 요청
     *
     * @param req 자동이체 생성 요청 정보
     * @return CoreAutoTransferRes (autoTransferId)
     */
    public CoreAutoTransferRes createAutoTransfer(CoreAutoTransferReq req){
        return coreRestTemplate.postForObject(
                properties.getBaseUrl() + AUTO_TRANSFER_CREATE_URL,
                req,
                CoreAutoTransferRes.class
        );
    }

    public void updateAutoTransfer(Long autoTransferId, CoreAutoTransferReq req){
        HttpEntity<CoreAutoTransferReq> entity = new HttpEntity<>(req);
        coreRestTemplate.exchange(
                properties.getBaseUrl() + AUTO_TRANSFER_UPDATE_URL,
                HttpMethod.PUT,
                entity,
                Void.class,
                autoTransferId
        );
    }
}
