package dev.syntax.domain.transfer.client;

import org.springframework.http.HttpEntity;
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

    private static final String AUTO_TRANSFER_URL = "/core/banking/account/auto-transfer/create";

    /**
     * Core 자동이체 생성 요청
     *
     * @param userId CoreUserId (부모)
     * @param req 자동이체 생성 요청 정보
     * @return CoreAutoTransferRes (autoTransferId)
     */
    public CoreAutoTransferRes createAutoTransfer(Long userId, CoreAutoTransferReq req){
        
        return coreRestTemplate.postForObject(
                properties.getBaseUrl() + AUTO_TRANSFER_URL,
                req,
                CoreAutoTransferRes.class
        );
    }
}
