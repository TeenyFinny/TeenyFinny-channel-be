package dev.syntax.domain.transfer.dto;
import java.math.BigDecimal;

import lombok.Builder;

/**
 * Core 서버에 자동이체 생성 요청을 전달하기 위한 DTO.
 *
 * <p>
 * 채널 서버(TeenyFinny 채널 API)가 CoreBank 서버로 자동이체 생성을 요청할 때 사용된다.
 * 이 DTO는 출금 계좌, 입금 계좌, 금액, 메모, 실행일 등의 자동이체 정보를 전달한다.
 * </p>
 *
 * <p>
 * Core 서버는 이 요청을 기반으로 자동이체 엔티티를 생성하고,
 * 다음 실행일(nextTransferDay)을 계산한 후 auto_transfer 테이블에 저장한다.
 * </p>
 *
 * @param userId         CoreUser 기준 자녀의 user_id (SecurityContext에서 검증된 사용자)
 * @param fromAccountId  자동이체 출금 계좌 ID (부모 계좌/자녀 계좌)
 * @param toAccountId    자동이체 입금 계좌 ID (자녀 계좌)
 * @param amount         자동이체 금액
 * @param transferDay    매월 실행될 날짜 (예: 5 → 매월 5일)
 * @param memo           자동이체 구분 메모 (예: "용돈", "투자", "목표")
 */
@Builder
public record CoreCreateAutoTransferReq (

    Long userId,          
    Long fromAccountId,   
    Long toAccountId,    
    BigDecimal amount,
    Integer transferDay,
    String memo
){}
