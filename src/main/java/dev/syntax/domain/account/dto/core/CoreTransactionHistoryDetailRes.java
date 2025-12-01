package dev.syntax.domain.account.dto.core;

import java.math.BigDecimal;
import java.util.List;

/**
 * 계좌의 전체 거래 내역 및 현재 잔액을 담는 응답 DTO
 * <p>
 * Core Banking 시스템에서 특정 계좌번호 조회 시 반환되는 최종 응답 형태입니다.
 * </p>
 *
 * <h3>포함 정보</h3>
 * <ul>
 *     <li>{@code transactions} — 거래 내역 리스트 (최신순)</li>
 *     <li>{@code balance} — 현재 계좌 잔액</li>
 * </ul>
 *
 * @param transactions 거래 내역 목록
 * @param balance 계좌 현재 잔액
 */
public record CoreTransactionHistoryDetailRes (
        List<CoreTransactionDetailItemRes> transactions,
        BigDecimal balance
) { }