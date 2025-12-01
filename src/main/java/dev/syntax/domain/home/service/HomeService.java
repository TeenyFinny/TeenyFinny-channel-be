package dev.syntax.domain.home.service;

import dev.syntax.domain.home.dto.HomeRes;
import dev.syntax.global.auth.dto.UserContext;

/**
 * 홈 화면 데이터 조회 비즈니스 로직을 정의하는 인터페이스입니다.
 */
public interface HomeService {

	/**
	 * 사용자를 기반으로 홈 화면 데이터를 조회합니다.
	 *
	 * @param context 인증된 사용자 컨텍스트
	 * @return 홈 화면 데이터 응답
	 */
	HomeRes getHomeData(UserContext context);
}
