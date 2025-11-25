package dev.syntax.domain.profile.service;

import org.springframework.stereotype.Service;

import dev.syntax.domain.profile.dto.ProfileInfoRes;
import dev.syntax.domain.profile.dto.UpdateProfileReq;
import dev.syntax.domain.profile.dto.UserProfile;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

	private final UserRepository userRepository;

	@Override
	public ProfileInfoRes profileInfo(UserContext user) {
		log.info("[프로필 상세 조회 성공] userId: {}", user.getId());
		return new ProfileInfoRes(new UserProfile(
			user.getUser().getName(),
			user.getEmail(),
			user.getUser().getPhoneNumber()
		));
	}

	@Override
	@Transactional
	public void updateProfile(UserContext userContext, UpdateProfileReq req) {
		// DB에서 User 엔티티를 다시 조회 (영속성 컨텍스트에서 관리되는 엔티티)
		User user = userRepository.findById(userContext.getId())
			.orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

		// 제공된 필드만 업데이트 (null이 아닌 경우에만)
		if (req.name() != null) {
			user.updateName(req.name());
		}
		if (req.phoneNumber() != null) {
			user.updatePhoneNumber(req.phoneNumber());
		}

		// @Transactional에 의해 트랜잭션 종료 시 자동으로 DB에 반영됨 (dirty checking)
		log.info("[프로필 수정 성공] userId: {}", user.getId());
	}
}
