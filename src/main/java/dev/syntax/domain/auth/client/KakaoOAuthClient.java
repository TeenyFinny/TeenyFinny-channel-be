package dev.syntax.domain.auth.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import dev.syntax.domain.auth.dto.oauth.KakaoTokenRes;
import dev.syntax.domain.auth.dto.oauth.KakaoUserInfo;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카카오 OAuth API 통신 클라이언트
 * 
 * 카카오 인증 서버 및 API 서버와 통신하여
 * 액세스 토큰 발급 및 사용자 정보를 조회합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

	private final RestTemplate restTemplate;

	@Value("${kakao.client-id}")
	private String clientId;

	@Value("${kakao.token-uri}")
	private String tokenUri;

	@Value("${kakao.user-info-uri}")
	private String userInfoUri;

	/**
	 * Authorization Code를 사용하여 카카오 액세스 토큰을 발급받습니다.
	 *
	 * @param code        카카오 인증 코드
	 * @param redirectUri 리다이렉트 URI
	 * @return 카카오 토큰 정보
	 */
	public KakaoTokenRes getAccessToken(String code, String redirectUri) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("grant_type", "authorization_code");
			params.add("client_id", clientId);
			params.add("redirect_uri", redirectUri);
			params.add("code", code);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

			ResponseEntity<KakaoTokenRes> response = restTemplate.postForEntity(
				tokenUri,
				request,
				KakaoTokenRes.class
			);

			if (response.getBody() == null) {
				throw new BusinessException(ErrorBaseCode.TOKEN_ISSUE_FAILED);
			}

			log.info("[카카오 토큰 발급 성공]");
			return response.getBody();
		} catch (Exception e) {
			log.error("[카카오 토큰 발급 실패] code: {}, error: {}", code, e.getMessage());
			throw new BusinessException(ErrorAuthCode.TOKEN_VALIDATION_FAILED);
		}
	}

	/**
	 * 카카오 액세스 토큰을 사용하여 사용자 정보를 조회합니다.
	 *
	 * @param accessToken 카카오 액세스 토큰
	 * @return 카카오 사용자 정보
	 */
	public KakaoUserInfo getUserInfo(String accessToken) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

			HttpEntity<Void> request = new HttpEntity<>(headers);

			ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
				userInfoUri,
				HttpMethod.GET,
				request,
				KakaoUserInfo.class
			);

			if (response.getBody() == null) {
				throw new BusinessException(ErrorBaseCode.USER_INFO_REQUEST_FAILED);
			}

			log.info("[카카오 사용자 정보 조회 성공] kakao_id: {}", response.getBody().id());
			return response.getBody();
		} catch (Exception e) {
			log.error("[카카오 사용자 정보 조회 실패] error: {}", e.getMessage());
			throw new BusinessException(ErrorBaseCode.USER_INFO_PARSE_FAILED);
		}
	}
}
