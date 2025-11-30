package dev.syntax.domain.user.repository;

import dev.syntax.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);

	/**
	 * 사용자 ID로 조회하면서 children 관계를 fetch join으로 함께 로드합니다.
	 * UserContext 생성 시 Lazy Loading 문제를 방지하기 위해 사용됩니다.
	 * DISTINCT를 사용하여 중복을 제거합니다.
	 *
	 * @param id 사용자 ID
	 * @return children이 로드된 User 엔티티
	 */
	@Query("SELECT DISTINCT u FROM User u " +
		"LEFT JOIN FETCH u.children c " +
		"LEFT JOIN FETCH c.child " +
		"WHERE u.id = :id")
	Optional<User> findByIdWithChildren(@Param("id") Long id);

	/**
	 * 사용자 ID로 조회하면서 parents 관계를 fetch join으로 함께 로드합니다.
	 * UserContext 생성 시 Lazy Loading 문제를 방지하기 위해 사용됩니다.
	 *
	 * @param id 사용자 ID
	 * @return parents가 로드된 User 엔티티
	 */
	@Query("SELECT DISTINCT u FROM User u " +
		"LEFT JOIN FETCH u.parents p " +
		"LEFT JOIN FETCH p.parent " +
		"WHERE u.id = :id")
	Optional<User> findByIdWithParents(@Param("id") Long id);

	/**
	 * 이메일로 조회하면서 children 관계를 fetch join으로 함께 로드합니다.
	 * UserContext 생성 시 Lazy Loading 문제를 방지하기 위해 사용됩니다.
	 * DISTINCT를 사용하여 중복을 제거합니다.
	 *
	 * @param email 사용자 이메일
	 * @return children이 로드된 User 엔티티
	 */
	@Query("SELECT DISTINCT u FROM User u " +
		"LEFT JOIN FETCH u.children c " +
		"LEFT JOIN FETCH c.child " +
		"WHERE u.email = :email")
	Optional<User> findByEmailWithChildren(@Param("email") String email);

	/**
	 * 이메일로 조회하면서 parents 관계를 fetch join으로 함께 로드합니다.
	 * UserContext 생성 시 Lazy Loading 문제를 방지하기 위해 사용됩니다.
	 *
	 * @param email 사용자 이메일
	 * @return parents가 로드된 User 엔티티
	 */
	@Query("SELECT DISTINCT u FROM User u " +
		"LEFT JOIN FETCH u.parents p " +
		"LEFT JOIN FETCH p.parent " +
		"WHERE u.email = :email")
	Optional<User> findByEmailWithParents(@Param("email") String email);

	/**
	 * providerId로 사용자를 조회합니다.
	 * 카카오 OAuth 로그인 시 사용됩니다.
	 *
	 * @param providerId 소셜 로그인 제공자 ID (예: kakao_123456789)
	 * @return User 엔티티
	 */
	@Query("SELECT DISTINCT u FROM User u " +
		"LEFT JOIN FETCH u.children c " +
		"LEFT JOIN FETCH c.child " +
		"WHERE u.providerId = :providerId")
	Optional<User> findByProviderIdWithChildren(@Param("providerId") String providerId);

	/**
	 * 전화번호, 생년월일, 이름으로 사용자를 조회합니다.
	 * ID 찾기 시 사용됩니다.
	 *
	 * @param phoneNumber 전화번호
	 * @param birthDate 생년월일 (LocalDate)
	 * @param name 이름
	 * @return User 엔티티
	 */
	Optional<User> findByPhoneNumberAndBirthDateAndName(
			@Param("phoneNumber") String phoneNumber,
			@Param("birthDate") java.time.LocalDate birthDate,
			@Param("name") String name
	);

	/**
	 * 이메일, 전화번호, 생년월일, 이름으로 사용자를 조회합니다.
	 * 비밀번호 재설정 시 사용됩니다.
	 *
	 * @param email 이메일
	 * @param phoneNumber 전화번호
	 * @param birthDate 생년월일 (LocalDate)
	 * @param name 이름
	 * @return User 엔티티
	 */
	Optional<User> findByEmailAndPhoneNumberAndBirthDateAndName(
			@Param("email") String email,
			@Param("phoneNumber") String phoneNumber,
			@Param("birthDate") java.time.LocalDate birthDate,
			@Param("name") String name
	);
}
