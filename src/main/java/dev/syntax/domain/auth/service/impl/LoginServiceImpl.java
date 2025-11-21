package dev.syntax.domain.auth.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import dev.syntax.domain.auth.dto.LoginReq;
import dev.syntax.domain.auth.dto.LoginRes;
import dev.syntax.domain.auth.dto.UserContext;
import dev.syntax.domain.auth.service.LoginService;
import dev.syntax.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public LoginRes login(LoginReq request) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.email(), request.password())
		);
		// 토큰 발급
		String accessToken = jwtTokenProvider.generateToken(authentication);
		UserContext userContext = (UserContext)authentication.getPrincipal();
		log.info("UserContext: {}", userContext.toString());
		log.info("로그인 성공: userId = {}", userContext.getId());
		return LoginRes.of(userContext, accessToken);
	}
}
