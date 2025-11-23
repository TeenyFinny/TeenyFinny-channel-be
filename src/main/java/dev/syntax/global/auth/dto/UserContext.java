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
 * 인증된 사용자 정보를 SecurityContext에 저장하기 위한 인증 모델입니다.
 *
 * <p>User 엔티티를 내부에 그대로 보관하며(children/parents는 fetch join으로 로딩됨),
 * 서비스 계층(HomeService 등)에서 User 엔티티를 직접 사용해도 N+1 문제가 발생하지 않도록 설계되어 있습니다.
 *
 * <h2>담고 있는 정보</h2>
 * <ul>
 *     <li><b>user</b> – 원본 User 엔티티 (children/parents 포함)</li>
 *     <li><b>id</b> – 사용자 PK (JWT subject로 사용)</li>
 *     <li><b>email</b> – 사용자 이메일</li>
 *     <li><b>password</b> – bcrypt 해시 비밀번호</li>
 *     <li><b>role</b> – 사용자 역할(PARENT / CHILD)</li>
 *     <li><b>familyId</b> – 가족 단위 기준 ID (부모: 본인 ID / 자녀: 부모 ID)</li>
 *     <li><b>parentId</b> – 자녀일 경우 부모 ID</li>
 *     <li><b>children</b> – 부모일 경우 자녀 ID 목록</li>
 *     <li><b>authorities</b> – Spring Security 인가 처리를 위한 권한 리스트</li>
 * </ul>
 *
 * <h2>역할</h2>
 * <ul>
 *     <li>로그인 성공 시 Authentication의 Principal로 저장</li>
 *     <li>JWT 재인증 시 DB에서 User를 재조회하여 최신 UserContext 생성</li>
 *     <li>서비스 계층에서 @CurrentUser로 직접 UserContext를 주입받아 사용</li>
 * </ul>
 *
 * <p>이 클래스는 User 엔티티를 그대로 보관하므로,
 * HomeService 등에서 userRepository.findById()를 다시 호출할 필요가 없으며
 * Lazy Loading 및 N+1 문제를 원천적으로 차단합니다.</p>
 *
 * @see org.springframework.security.core.userdetails.UserDetails
 * @see dev.syntax.domain.user.entity.User
 */
@Getter
public class UserContext implements UserDetails {

	private final User user;

	private final Long id;
	private final String email;
	private final String password;   // bcrypt 해시
	private final String role;

	private final Long familyId;
	private final Long parentId;
	private final List<Long> children;

	private final Collection<? extends GrantedAuthority> authorities;

	public UserContext(User user) {
		this.user = user;
		this.id = user.getId();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.role = user.getRole().name();

		this.children = user.getChildren() != null
			? user.getChildren().stream()
			.map(UserRelationship::getChild)
			.filter(child -> child != null)  // child가 null인 경우 제외 (OTP 대기 중인 관계)
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
