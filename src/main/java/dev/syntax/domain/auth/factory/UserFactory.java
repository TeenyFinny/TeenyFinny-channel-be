package dev.syntax.domain.auth.factory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.security.crypto.password.PasswordEncoder;

import dev.syntax.domain.auth.dto.SignupReq;
import dev.syntax.domain.user.entity.User;

/**
 * 회원가입 과정에서 사용자(User) 엔티티를 생성하기 위한 팩토리 클래스입니다.
 */
public class UserFactory {

	// 인스턴스화 금지
	private UserFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * 회원가입 요청 정보를 기반으로 User 엔티티를 생성합니다.
	 * <p>
	 * 패스워드 및 간편 비밀번호는 전달된 {@link PasswordEncoder}를 통해 암호화되며,
	 * 생년월일은 "yyyyMMdd" 형식의 문자열을 {@link LocalDate}로 변환하여 저장합니다.
	 *
	 * @param req     회원가입 요청 DTO
	 * @param encoder 비밀번호 암호화에 사용되는 {@link PasswordEncoder}
	 * @return 생성된 User 엔티티
	 */
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
