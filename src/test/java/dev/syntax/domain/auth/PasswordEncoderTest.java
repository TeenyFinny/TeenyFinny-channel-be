package dev.syntax.domain.auth;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordEncoderTest {

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Test
	void bcrypt_비밀번호_인코딩_및_매칭_성공() {
		// given
		String raw = "testPassword123!";

		// when
		String encoded = passwordEncoder.encode(raw);

		// then
		assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
	}

	@Test
	void bcrypt_서로다른_salt_해시_비교_실패() {
		// given
		String raw = "simplePw123";
		String encoded1 = passwordEncoder.encode(raw);
		String encoded2 = passwordEncoder.encode(raw);

		// then
		assertThat(encoded1).isNotEqualTo(encoded2); // salt가 달라야 함
		assertThat(passwordEncoder.matches(raw, encoded1)).isTrue();
		assertThat(passwordEncoder.matches(raw, encoded2)).isTrue();
	}
}
