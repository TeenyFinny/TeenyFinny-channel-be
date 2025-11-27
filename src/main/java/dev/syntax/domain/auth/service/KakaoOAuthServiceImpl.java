package dev.syntax.domain.auth.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.auth.client.KakaoOAuthClient;
import dev.syntax.domain.auth.dto.LoginRes;
import dev.syntax.domain.auth.dto.UserLoginInfo;
import dev.syntax.domain.auth.dto.oauth.KakaoLoginReq;
import dev.syntax.domain.auth.dto.oauth.KakaoLoginRes;
import dev.syntax.domain.auth.dto.oauth.KakaoSignupReq;
import dev.syntax.domain.auth.dto.oauth.KakaoTokenRes;
import dev.syntax.domain.auth.dto.oauth.KakaoUserInfo;
import dev.syntax.domain.auth.entity.KakaoTempToken;
import dev.syntax.domain.auth.repository.KakaoTempTokenRepository;
import dev.syntax.domain.user.client.CoreUserClient;
import dev.syntax.domain.user.dto.CoreUserInitReq;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.auth.jwt.JwtTokenProvider;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카카오 OAuth 인증 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthServiceImpl implements KakaoOAuthService {

	private final KakaoOAuthClient kakaoOAuthClient;
	private final UserRepository userRepository;
	private final KakaoTempTokenRepository kakaoTempTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final CoreUserClient coreUserClient;

	private static final String DEFAULT_TOKEN_TYPE = "Bearer";
	private static final int TEMP_TOKEN_EXPIRATION_MINUTES = 5; // 10분 → 5분으로 단축

	@Override
	@Transactional
	public KakaoLoginRes loginWithKakao(KakaoLoginReq request) {
		// 1. 카카오 액세스 토큰 획득
		KakaoTokenRes kakaoToken = kakaoOAuthClient.getAccessToken(
			request.code(),
			request.redirectUri()
		);

		// 2. 카카오 사용자 정보 조회
		KakaoUserInfo kakaoUserInfo = kakaoOAuthClient.getUserInfo(kakaoToken.accessToken());
		String providerId = kakaoUserInfo.getProviderId();

		// 3. DB에서 providerId로 사용자 확인
		User user = userRepository.findByProviderIdWithChildren(providerId).orElse(null);

		if (user != null) {
			// 기존 사용자: JWT 발급
			log.info("[카카오 로그인 성공] user_id: {}, providerId: {}", user.getId(), providerId);
			return createExistingUserResponse(user);
		} else {
			// 신규 사용자: 임시 토큰 발급
			log.info("[카카오 신규 사용자] providerId: {}", providerId);
			return createNewUserResponse(kakaoUserInfo);
		}
	}

	@Override
	@Transactional
	public LoginRes signupWithKakao(KakaoSignupReq request) {
		// 1. 임시 토큰 검증 및 조회
		KakaoTempToken tempToken = kakaoTempTokenRepository.findByToken(request.tempToken())
			.orElseThrow(() -> new BusinessException(ErrorBaseCode.UNAUTHORIZED));

		// 만료 확인
		if (tempToken.isExpired()) {
			kakaoTempTokenRepository.delete(tempToken);
			throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
		}

		// 2. providerId 중복 확인
		if (userRepository.findByProviderIdWithChildren(tempToken.getProviderId()).isPresent()) {
			throw new BusinessException(ErrorAuthCode.KAKAO_PROVIDER_CONFLICT);
		}

		// 3. 이메일 중복 확인 (카카오 이메일이 있는 경우)
		if (tempToken.getKakaoEmail() != null &&
			userRepository.existsByEmail(tempToken.getKakaoEmail())) {
			throw new BusinessException(ErrorAuthCode.KAKAO_EMAIL_CONFLICT);
		}

		// 4. User 엔티티 생성
		User user = createKakaoUser(request, tempToken);
		User savedUser = userRepository.save(user);

		// 5. Core 시스템 연동
		try {
			CoreUserInitReq coreUserReq = new CoreUserInitReq(
				savedUser.getId(),
				savedUser.getRole(),
				savedUser.getName(),
				savedUser.getPhoneNumber(),
				savedUser.getBirthDate()
			);

			Long coreUserId;
			if (savedUser.getRole() == Role.PARENT) {
				coreUserId = coreUserClient.createParentAccount(coreUserReq).coreUserId();
			} else {
				coreUserId = coreUserClient.createChildUser(coreUserReq).coreUserId();
			}

			savedUser.setCoreUserId(coreUserId);
			log.info("[Core 사용자 생성 완료] channel_user_id: {}, core_user_id: {}",
				savedUser.getId(), coreUserId);
		} catch (Exception e) {
			log.error("[Core 사용자 생성 실패] user_id: {}, error: {}",
				savedUser.getId(), e.getMessage());
			throw new BusinessException(ErrorBaseCode.INTERNAL_SERVER_ERROR);
		}

		// 6. 임시 토큰 삭제
		kakaoTempTokenRepository.delete(tempToken);

		// 7. JWT 발급
		log.info("[카카오 회원가입 완료] user_id: {}, providerId: {}",
			savedUser.getId(), savedUser.getProviderId());

		return createLoginResponse(savedUser);
	}

	/**
	 * 기존 사용자 응답 생성
	 */
	private KakaoLoginRes createExistingUserResponse(User user) {
		UserContext userContext = UserContext.from(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userContext,
			null,
			userContext.getAuthorities()
		);

		String accessToken = jwtTokenProvider.generateToken(authentication);

		UserLoginInfo userLoginInfo = UserLoginInfo.of(user);

		return KakaoLoginRes.forExistingUser(
			userLoginInfo,
			DEFAULT_TOKEN_TYPE,
			accessToken
		);
	}

	/**
	 * 신규 사용자 응답 생성 (임시 토큰 발급)
	 */
	private KakaoLoginRes createNewUserResponse(KakaoUserInfo kakaoUserInfo) {
		String tokenValue = UUID.randomUUID().toString();
		
		// 기존 providerId의 토큰 삭제 (중복 방지)
		kakaoTempTokenRepository.deleteByProviderId(kakaoUserInfo.getProviderId());
		
		// DB에 임시 토큰 저장
		KakaoTempToken tempToken = KakaoTempToken.builder()
			.token(tokenValue)
			.providerId(kakaoUserInfo.getProviderId())
			.kakaoEmail(kakaoUserInfo.getEmail())
			.kakaoName(kakaoUserInfo.getNickname())
			.expiresAt(LocalDateTime.now().plusMinutes(TEMP_TOKEN_EXPIRATION_MINUTES))
			.build();
		
		kakaoTempTokenRepository.save(tempToken);

		return KakaoLoginRes.forNewUser(
			tokenValue,
			kakaoUserInfo.getEmail(),
			kakaoUserInfo.getNickname()
		);
	}

	/**
	 * 카카오 사용자 엔티티 생성
	 */
	private User createKakaoUser(KakaoSignupReq request, KakaoTempToken tempToken) {
		// 이메일: 카카오 이메일 우선, 없으면 providerId 기반 생성
		String email = tempToken.getKakaoEmail() != null
			? tempToken.getKakaoEmail()
			: tempToken.getProviderId() + "@kakao.local";

		// 비밀번호: 카카오 로그인은 비밀번호 불필요하므로 랜덤 생성
		String randomPassword = UUID.randomUUID().toString();

		return User.builder()
			.name(request.name())
			.email(email)
			.phoneNumber(request.phoneNumber())
			.password(passwordEncoder.encode(randomPassword))
			.simplePassword(passwordEncoder.encode(request.simplePassword()))
			.birthDate(LocalDate.parse(request.birthDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")))
			.gender(request.gender())
			.role(Role.valueOf(request.role()))
			.providerId(tempToken.getProviderId())
			.build();
	}

	/**
	 * 로그인 응답 생성
	 */
	private LoginRes createLoginResponse(User user) {
		UserContext userContext = UserContext.from(user);
		Authentication authentication = new UsernamePasswordAuthenticationToken(
			userContext,
			null,
			userContext.getAuthorities()
		);

		String accessToken = jwtTokenProvider.generateToken(authentication);

		return LoginRes.of(userContext, accessToken);
	}
}
