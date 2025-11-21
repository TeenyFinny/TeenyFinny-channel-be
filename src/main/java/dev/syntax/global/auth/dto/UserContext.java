package dev.syntax.global.auth.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.entity.UserRelationship;
import lombok.Getter;

/**
 * UserContext
 *
 * <p>Spring Security의 UserDetails 구현체로,
 * 인증된 사용자 정보를 SecurityContext에서 유지하기 위한 DTO입니다.
 * User 엔티티의 핵심 정보(권한, 가족 관계 등)를 인증 객체 형태로 변환하며,
 * JWT 인증 후에도 재구성 가능한 형태로 사용됩니다.
 *
 * <h2>담고 있는 정보</h2>
 * <ul>
 *     <li><b>id</b> – 사용자 PK (JWT subject로 사용)</li>
 *     <li><b>email</b> – 사용자 이메일</li>
 *     <li><b>password</b> – bcrypt 해시 비밀번호 (로그인 검증 시 사용)</li>
 *     <li><b>role</b> – 사용자 역할(PARENT / CHILD)</li>
 *     <li><b>familyId</b> – 가족 그룹 기준 ID (부모: 본인, 자녀: 부모 ID)</li>
 *     <li><b>parentId</b> – 자녀 계정일 경우 부모 ID</li>
 *     <li><b>children</b> – 부모 계정일 경우 자녀 ID 목록</li>
 *     <li><b>authorities</b> – Spring Security 인가에 사용할 권한 값(ROLE_ prefix 포함)</li>
 * </ul>
 *
 * <h2>역할</h2>
 * <ul>
 *     <li>로그인 성공 시 Authentication 객체의 Principal로 저장</li>
 *     <li>JWT 검증 후 DB 재조회로 최신 UserContext 재구성</li>
 *     <li>Spring Security 인가 처리(@PreAuthorize, hasRole 등)에서 사용</li>
 * </ul>
 *
 * @see org.springframework.security.core.userdetails.UserDetails
 * @see dev.syntax.domain.user.entity.User
 */
@Getter
public class UserContext implements UserDetails {

	private final Long id;
	private final String email;
	private final String password;   // bcrypt 해시
	private final String role;

	private final Long familyId;
	private final Long parentId;
	private final List<Long> children;

	private final Collection<? extends GrantedAuthority> authorities;

	public UserContext(User user) {
		this.id = user.getId();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.role = user.getRole().name();

		this.children = user.getChildren() != null
			? user.getChildren().stream()
			.map(UserRelationship::getChild)
			.map(User::getId)
			.collect(Collectors.toList())
			: Collections.emptyList();

		this.parentId = user.getParents() != null && !user.getParents().isEmpty()
			? user.getParents().get(0).getParent().getId()
			: null;

		this.familyId = (parentId == null) ? this.id : parentId;

		this.authorities = Collections.singleton(
			new SimpleGrantedAuthority("ROLE_" + role)
		);
	}

	@Override
	public String getUsername() {
		// JWT subject로 userId를 쓰기 위해 id 반환
		return String.valueOf(id);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
