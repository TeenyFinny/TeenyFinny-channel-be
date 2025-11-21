package dev.syntax.domain.auth.factory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.security.crypto.password.PasswordEncoder;

import dev.syntax.domain.auth.dto.SignupReq;
import dev.syntax.domain.user.entity.User;

public class UserFactory {

	// 인스턴스화 금지
	private UserFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static User create(SignupReq req, PasswordEncoder encoder) {

		return User.builder()
			.name(req.name())
			.email(req.email())
			.phoneNumber(req.phoneNumber())
			.password(encoder.encode(req.password()))
			.simplePassword(encoder.encode(req.simplePassword()))
			.birthDate(LocalDate.parse(req.birthDate(), DateTimeFormatter.ofPattern("yyyyMMdd")))
			.gender(req.gender().byteValue())
			.role(req.role())
			.build();
	}
}
