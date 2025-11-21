package dev.syntax.global.auth.jwt.test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;

public class TestAuthenticationFactory {

	public static UsernamePasswordAuthenticationToken createAuth() {
		User user = User.builder()
			.id(1L)
			.email("test@abc.com")
			.password("encodedPw") // bcrypt일 필요 없음 (토큰 생성에서 사용되지 않음)
			.role(Role.PARENT)
			.build();

		UserContext context = new UserContext(user);

		return new UsernamePasswordAuthenticationToken(
			context,
			null,
			context.getAuthorities()
		);
	}
}