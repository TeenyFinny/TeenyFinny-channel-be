package dev.syntax.domain.transfer.dto;

/**
 * Core 서버에서 자동이체 생성 후 반환되는 응답 DTO입니다.
 * @param autoTransferId Core 서버에 생성된 자동이체 ID
 */ 
public record CoreCreateAutoTransferRes (
    Long autoTransferId
){}
