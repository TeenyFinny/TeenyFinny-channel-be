package dev.syntax.domain.user.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.common.BaseTimeEntity;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_service")
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

	@Column(name = "provider_id", length = 255)
	private String providerId;

	@Setter
	@Column(name = "core_user_id")
	private Long coreUserId;

	@Builder.Default
	@OneToMany(mappedBy = "parent")
	private List<UserRelationship> children = new ArrayList<>();

	@Builder.Default
	@OneToMany(mappedBy = "child")
	private List<UserRelationship> parents = new ArrayList<>();

	public void updateName(String name) {
		validateNotBlank(name);
		this.name = name;
	}

	public void updatePhoneNumber(String phoneNumber) {
		validateNotBlank(phoneNumber);
		this.phoneNumber = phoneNumber;
	}

	/**
	 * 비밀번호를 업데이트합니다.
	 *
	 * @param password 새로운 비밀번호 (암호화된 상태)
	 */
	public void updatePassword(String password) {
		validateNotBlank(password);
		this.password = password;
	}

	/**
	 * 간편 비밀번호를 업데이트합니다.
	 *
	 * @param simplePassword 새로운 간편 비밀번호 (암호화된 상태)
	 */
	public void updateSimplePassword(String simplePassword) {
		validateNotBlank(simplePassword);
		this.simplePassword = simplePassword;
	}

	/**
	 * 푸시 알림 설정을 업데이트합니다.
	 *
	 * @param pushEnabled 푸시 알림 활성화 여부
	 */
	public void updatePushEnabled(Boolean pushEnabled) {
		requireNonNull(pushEnabled);
		this.pushEnabled = pushEnabled;
	}

	/**
	 * 야간 푸시 알림 설정을 업데이트합니다.
	 *
	 * @param nightPushEnabled 야간 푸시 알림 활성화 여부
	 */
	public void updateNightPushEnabled(Boolean nightPushEnabled) {
		requireNonNull(nightPushEnabled);
		this.nightPushEnabled = nightPushEnabled;
	}

	private void validateNotBlank(String value) {
		if (value == null || value.isBlank()) {
			throw new BusinessException(ErrorBaseCode.BAD_REQUEST);
		}
	}

	private void requireNonNull(Object value) {
		if (value == null) {
			throw new BusinessException(ErrorBaseCode.BAD_REQUEST);
		}
	}
}
