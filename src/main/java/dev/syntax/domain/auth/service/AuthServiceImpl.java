package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.auth.jwt.JwtTokenProvider;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

	@Override
	public void checkEmailDuplicate(EmailValidationReq request) {
		boolean exists = userRepository.existsByEmail(request.email());
		if (exists) {
			throw new BusinessException(ErrorAuthCode.EMAIL_CONFLICT);
		}
	}

	@Override
	public RefreshTokenRes refreshToken(Long userId) {
		// 최신 User 정보 조회 (가족 관계 포함)
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new BusinessException(ErrorAuthCode.UNAUTHORIZED));

		// 최신 정보로 UserContext 생성
		UserContext userContext = new UserContext(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
				userContext,
				null,
				userContext.getAuthorities()
		);

		// 새 JWT 토큰 발급
		String newAccessToken = jwtTokenProvider.generateToken(authentication);

		return RefreshTokenRes.builder()
				.accessToken(newAccessToken)
				.build();
	}

    @Override
    public PasswordVerifyRes verifyPassword(Long userId, PasswordVerifyReq request) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorAuthCode.UNAUTHORIZED));

        // 2. 비밀번호 비교 (PasswordEncoder 사용)
        boolean matched = passwordEncoder.matches(request.password(), user.getPassword());

        if (!matched) {
            throw new BusinessException(ErrorAuthCode.PASSWORD_MISMATCH);
            // == "AUTH04", "패스워드를 확인해주세요."
        }

        // 3. 성공 시 반환
        return new PasswordVerifyRes(true);
    }

    @Override
    public PasswordVerifyRes verifySimplePassword(Long userId, SimplePasswordVerifyReq request) {

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorAuthCode.UNAUTHORIZED));

        // 간편비밀번호 비교 (DB에 저장된 간편비밀번호가 있다고 가정)
        boolean matched = passwordEncoder.matches(request.password(), user.getSimplePassword());

        if (!matched) {
            throw new BusinessException(ErrorAuthCode.SIMPLE_PASSWORD_MISMATCH); // 새 에러코드
        }

        return new PasswordVerifyRes(true);
    }

    @Override
    public IdentityVerifyRes verifyIdentity(Long userId, IdentityVerifyReq request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorAuthCode.UNAUTHORIZED));

        // 2. 생년월일 앞자리 (YYMMDD)
        String birthFront = user.getBirthDate().format(DateTimeFormatter.ofPattern("yyMMdd"));

        // 3. 뒷자리 계산 (성별 + 출생년도 기준)
        int year = user.getBirthDate().getYear(); // 4자리 연도
        Byte gender = user.getGender();          // 1=남, 2=여

        String birthBack = switch (gender) {
            case 1 -> year < 2000 ? "1" : "3"; // 남자
            case 2 -> year < 2000 ? "2" : "4"; // 여자
            default -> "0";                     // 예외
        };

        // 4. 인증 비교
        boolean verified = user.getName().equals(request.name())
                && user.getPhoneNumber().equals(request.phoneNumber())
                && birthFront.equals(request.birthFront())
                && birthBack.equals(request.birthBack());

        if (!verified) {
            throw new BusinessException(ErrorAuthCode.IDENTITY_MISMATCH);
        }

        // 5. 성공 시 반환
        return new IdentityVerifyRes(true, "인증 완료");
    }


}
