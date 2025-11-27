package dev.syntax.domain.transfer.entity;

import dev.syntax.domain.transfer.dto.AutoTransferReq;
import dev.syntax.domain.transfer.enums.AutoTransferFrequency;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "auto_transfer")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoTransfer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auto_transfer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "transfer_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal transferAmount;

    @Column(name = "ratio")
    private Integer ratio;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AutoTransferType type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 20)
    private AutoTransferFrequency frequency = AutoTransferFrequency.MONTHLY;

    @Column(name = "transfer_date")
    private Integer transferDate;


    /**
     * 코어 뱅킹 시스템에서 생성된 "기본 자동이체" 식별자.
     * (용돈/목표 자동이체)
     * null 허용되지 않음.
     */
    @Column(name = "primary_bank_transfer_id", nullable = false)
    private Long primaryBankTransferId;


    /**
     * 코어 뱅킹 시스템에서 생성된 "투자 자동이체" 식별자.
     * 투자 비율이 0일 경우 null.
     */
    @Column(name = "invest_bank_transfer_id")
    private Long investBankTransferId;

    public void updateAutoTransfer(AutoTransferReq newReq, Long newInvestTransferId){
        this.ratio = newReq.getRatio();
        this.transferDate = newReq.getTransferDate();
        this.transferAmount = newReq.getTotalAmount();
        this.investBankTransferId = newInvestTransferId;
    }
}
