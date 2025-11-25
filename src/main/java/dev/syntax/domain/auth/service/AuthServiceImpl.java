package dev.syntax.domain.auth.service;

import dev.syntax.domain.auth.dto.*;
import dev.syntax.global.auth.validator.IdentityValidator;
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

import java.time.LocalDate;
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
    public IdentityVerifyRes verifyIdentity(Long userId, IdentityVerifyReq req) {

        if (req == null) {
            throw new BusinessException(ErrorAuthCode.INVALID_IDENTITY_FORMAT);
        }

        // 1. 요청 Body 검증 (DTO → 추가 validator)
        IdentityValidator.validateCarrier(req.carrier());
        IdentityValidator.validatePhone(req.phoneNumber());
        IdentityValidator.validateBirth(req.birthFront(), req.birthBack());


        // 2. 성공 시 반환
        return new IdentityVerifyRes(true, "인증 완료");
    }


}
