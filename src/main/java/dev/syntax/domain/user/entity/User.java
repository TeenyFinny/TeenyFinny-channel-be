package dev.syntax.domain.user.entity;

import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "simple_password", nullable = false, length = 255)
    private String simplePassword;

    @Column(name = "salt", nullable = false, length = 255)
    private String salt;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "gender", nullable = false)
    private Byte gender; // 1: 남, 2: 여

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Builder.Default
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = true;

    @Builder.Default
    @Column(name = "night_push_enabled", nullable = false)
    private Boolean nightPushEnabled = false;

    @Column(name = "provider_id", length = 10)
    private String providerId;

    @OneToMany(mappedBy = "parent")
    private List<UserRelationship> children = new ArrayList<>();

    @OneToMany(mappedBy = "child")
    private List<UserRelationship> parents = new ArrayList<>();
}