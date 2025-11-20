package dev.syntax.domain.transfer.entity;

import dev.syntax.domain.transfer.enums.AutoTransferFrequency;
import dev.syntax.domain.transfer.enums.AutoTransferStatus;
import dev.syntax.domain.transfer.enums.AutoTransferType;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(name = "bank_transfer_id")
    private Long bankTransferId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private AutoTransferStatus status = AutoTransferStatus.PENDING;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;
}
