package dev.syntax.domain.profile.service;

import org.springframework.stereotype.Service;

import dev.syntax.domain.profile.dto.ProfileInfoRes;
import dev.syntax.domain.profile.dto.UserProfile;
import dev.syntax.global.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

	@Override
	public ProfileInfoRes profileInfo(UserContext user) {
		log.info("[프로필 상세 조회 성공] userId: {}", user.getId());
		return new ProfileInfoRes(new UserProfile(
			user.getUser().getName(),
			user.getEmail(),
			user.getUser().getPhoneNumber()
		));
	}
}
