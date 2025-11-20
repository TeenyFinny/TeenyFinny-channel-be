package dev.syntax.domain.user.repository;

import dev.syntax.domain.user.entity.UserRelationship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRelationshipRepository extends JpaRepository<UserRelationship,Long> {
}
