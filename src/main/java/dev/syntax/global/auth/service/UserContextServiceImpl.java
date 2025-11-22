package dev.syntax.global.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;

/**
 * UserContextServiceImpl
 *
 * <p>Spring Security의 UserDetailsService 구현체로,
 * 사용자 인증 시 User 엔티티를 조회하여 UserContext로 변환합니다.
 * UserContext 생성 시 children과 parents 관계를 fetch join으로 함께 로드하여
 * Lazy Loading 문제를 방지합니다.
 *
 * <p>MultipleBagFetchException을 방지하기 위해 children과 parents를
 * 별도 쿼리로 조회합니다.
 */
@Service
@RequiredArgsConstructor
public class UserContextServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	/**
	 * 이메일로 사용자를 조회하여 UserDetails를 반환합니다.
	 * children과 parents 관계를 fetch join으로 함께 로드합니다.
	 *
	 * @param username 사용자 이메일
	 * @return UserContext (UserDetails 구현체)
	 * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
	 */
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// children 먼저 로드
		User user = userRepository.findByEmailWithChildren(username)
			.orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));

		// parents 추가 로드 (이미 영속성 컨텍스트에 있으므로 merge됨)
		userRepository.findByEmailWithParents(username);

		return new UserContext(user);
	}

	/**
	 * 사용자 ID로 사용자를 조회하여 UserContext를 반환합니다.
	 * children과 parents 관계를 fetch join으로 함께 로드합니다.
	 *
	 * @param id 사용자 ID
	 * @return UserContext
	 * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
	 */
	@Transactional(readOnly = true)
	public UserContext loadUserById(Long id) {
		// children 먼저 로드
		User user = userRepository.findByIdWithChildren(id)
			.orElseThrow(() -> new UsernameNotFoundException(id + " -> 데이터베이스에서 찾을 수 없습니다."));

		// parents 추가 로드 (이미 영속성 컨텍스트에 있으므로 merge됨)
		userRepository.findByIdWithParents(id);

		return new UserContext(user);
	}
}
