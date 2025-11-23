package dev.syntax.domain.auth.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import dev.syntax.domain.auth.dto.EmailValidationReq;
import dev.syntax.domain.auth.dto.RefreshTokenRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.auth.jwt.JwtTokenProvider;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

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
}
