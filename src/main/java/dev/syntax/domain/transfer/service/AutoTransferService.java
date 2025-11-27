package dev.syntax.domain.transfer.service;

import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.global.auth.dto.UserContext;

public interface AutoTransferService {

    void deleteAutoTransfer(Long accountId, AutoTransferType type);
}
