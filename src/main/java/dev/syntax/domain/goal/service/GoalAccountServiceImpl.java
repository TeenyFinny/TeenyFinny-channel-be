package dev.syntax.domain.goal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreGoalAccountReq;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.goal.entity.Goal;
import dev.syntax.domain.goal.repository.GoalRepository;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * GoalAccountServiceImpl
 *
 * <p>사용자 목표 계좌 생성 관련 비즈니스 로직을 수행하는 서비스입니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoalAccountServiceImpl implements GoalAccountService {

	private final CoreAccountClient coreAccountClient;
	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	private final GoalRepository goalRepository;

	/**
	 * 목표 계좌를 생성합니다.
	 *
	 * <p>주요 처리 과정:</p>
	 * <ol>
	 *     <li>사용자 조회: userId로 User 엔티티 조회, 없으면 실패 반환</li>
	 *     <li>진행 중인 목표 존재 여부 검증: 이미 진행 중이면 실패 반환</li>
	 *     <li>코어 서버에 목표 계좌 생성 요청</li>
	 *     <li>계좌 정보 DB 저장</li>
	 *     <li>처리 도중 예외가 발생하면 false 반환, 정상 처리 시 true 반환</li>
	 * </ol>
	 *
	 * @param goal 목표 이름
	 * @return GoalAccountCreateRes 계좌 생성 성공 여부
	 */
	@Transactional
	public Goal createGoalAccount(Goal goal) {
		// 앞에서 모든 검증 마침
		CoreGoalAccountReq req = CoreGoalAccountReq.builder()
			.childCoreId(goal.getUser().getCoreUserId())
			.name(goal.getName())
			.build();
		log.info(req.toString());
		// core에 계좌 생성 요청
		CoreAccountItemRes coreRes = coreAccountClient.createGoalAccount(req);
		log.info("[CORE] 목표 계좌 생성 완료: userID={}, accountNumber={}", goal.getUser().getId(),
			coreRes.accountNumber());

		if (coreRes.accountNumber() == null) {
			throw new BusinessException(ErrorBaseCode.CREATE_FAILED);
		}

		// 4. 채널 DB에 계좌 정보 저장
		Account account = Account.builder()
			.user(goal.getUser())
			.type(AccountType.GOAL)
			.accountNo(coreRes.accountNumber())
			.build();
		accountRepository.save(account);

		// goal에 Account 연동 완료
		goal.updateAccount(account);

		log.info("[CHANNEL] 목표 계좌 생성 완료: userId={}, goalId={}, accountId={}, goalName={}", goal.getUser().getId(),
			goal.getId(), account.getId(), goal.getName());

		return goal;
	}
}
