package dev.syntax.domain.card.entity;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "card")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "number", nullable = false, length = 20)
    private String number;

    @Column(name = "cvc", nullable = false, length = 3)
    private String cvc;

    @Column(name = "password", nullable = false, length = 255)
    private String password;
}
