package dev.syntax.domain.user.repository;

import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRelationshipRepository extends JpaRepository<UserRelationship,Long> {
    Optional<UserRelationship> findByFamilyOtpAndChildIsNull(String familyOtp);
    Optional<UserRelationship> findByParentAndChildIsNull(User parent);

    Optional<UserRelationship> findByChild(User child);
}
