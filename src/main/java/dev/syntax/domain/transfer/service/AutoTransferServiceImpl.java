package dev.syntax.domain.transfer.service;

import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.transfer.client.CoreTransferClient;
import dev.syntax.domain.transfer.entity.AutoTransfer;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.transfer.repository.AutoTransferRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoTransferServiceImpl implements AutoTransferService {

    private final AutoTransferRepository autoTransferRepository;
    private final CoreTransferClient coreTransferClient;

    @Override
    @Transactional
    public void deleteAutoTransfer(Long accountId, AutoTransferType type) {

        AutoTransfer autoTransfer = autoTransferRepository.findByAccountIdAndType(accountId, type)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.AUTO_TRANSFER_NOT_FOUND));

        coreTransferClient.deleteAutoTransfer(autoTransfer.getPrimaryBankTransferId());
        autoTransferRepository.delete(autoTransfer);
    }
}
