package dev.syntax.domain.account.service;

import dev.syntax.domain.account.dto.CreateChildAccountReq;
import dev.syntax.domain.user.dto.CoreParentInitRes;
import dev.syntax.domain.user.entity.User;

/**
 * 계좌 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface BankAccountService {
	/**
	 * 부모 계좌를 생성합니다.
	 * @param user 사용자 정보
	 * @param res Core 서버 초기화 응답
	 */
	void creatParentAccount(User user, CoreParentInitRes res);

	/**
	 * 자녀의 용돈 계좌를 생성합니다.
	 * @param user 부모 사용자 정보
	 * @param res 자녀 계좌 생성 요청
	 */
	void createChildAllowanceAccount(User user, CreateChildAccountReq res);
}
