package dev.syntax.domain.auth.factory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import dev.syntax.domain.auth.dto.SignupReq;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;

public class UserFactory {

	// 인스턴스화 금지
	private UserFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static User create(SignupReq req) {

		Role role = Role.valueOf(req.role().toUpperCase());

		return User.builder()
			.name(req.name())
			.email(req.email())
			.phoneNumber(req.phoneNumber())
			.password(req.password())
			.simplePassword(req.simplePassword())
			.birthDate(LocalDate.parse(req.birthDate(), DateTimeFormatter.ofPattern("yyyyMMdd")))
			.gender(req.gender().byteValue())
			.role(role)
			.build();
	}
}
