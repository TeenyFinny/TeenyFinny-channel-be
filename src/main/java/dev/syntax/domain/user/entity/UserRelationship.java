package dev.syntax.domain.user.entity;

import dev.syntax.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_relationship")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRelationship extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relationship_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = true)
    private User child;

    @Column(name = "family_otp", nullable = false, length = 10)
    private String familyOtp;

    public static UserRelationship create(User parent, User child, String otp) {
        return UserRelationship.builder()
                .parent(parent)
                .child(child)
                .familyOtp(otp)
                .build();
    }
}
