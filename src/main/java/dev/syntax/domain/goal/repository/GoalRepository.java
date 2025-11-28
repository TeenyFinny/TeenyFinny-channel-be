package dev.syntax.domain.goal.repository;

import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.enums.GoalStatus;
import dev.syntax.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    boolean existsByUserAndStatus(User user, GoalStatus status);

    Optional<Goal> findByAccount_AccountNo(String accountNo);

    Optional<Goal> findByUserAndStatus(User user, GoalStatus status);
}
