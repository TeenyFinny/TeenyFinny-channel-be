package dev.syntax.domain.account.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.CreateChildAccountReq;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreCreateAccountReq;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.dto.CoreParentInitRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.domain.user.repository.UserRepository;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorAuthCode;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 계좌 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

	private final AccountRepository accountRepository;
	private final CoreAccountClient coreAccountClient;
	private final UserRepository userRepository;

	@Override
	public void creatParentAccount(User user, CoreParentInitRes res) {

		// 1) null 체크하거나 잘못된 데이터 체크하고
		if (res.account() == null) {
			throw new BusinessException(ErrorBaseCode.INVALID_REQUEST);
		}

		// 2) 계좌 타입 체크
		if (res.account().accountType() == null) {
			throw new BusinessException(ErrorBaseCode.INVALID_ACCOUNT_TYPE);
		}

		// 3) JPA persist 필요하면 repository.save() 호출
		Account account = Account.builder()
			.user(user)
			.type(res.account().accountType())
			.accountNo(res.account().accountNumber())
			.build();

		accountRepository.save(account);
	}

	@Override
	public void createChildAllowanceAccount(User parent, CreateChildAccountReq req) {

		if (parent.getRole() != Role.PARENT){
			throw new BusinessException(ErrorAuthCode.ACCESS_DENIED);
		}

		// 1) null 체크하거나 잘못된 데이터 체크하고
		if (req == null) {
			throw new BusinessException(ErrorBaseCode.INVALID_REQUEST);
		}

		// 2) 자녀 조회
		User child = userRepository.findById(req.childId())
				.orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));

		// 3) core용 DTO 생성
		CoreCreateAccountReq coreReq = CoreCreateAccountReq.builder()
				.parentCoreId(parent.getCoreUserId())
				.childCoreId(child.getCoreUserId())
				.build();
	
		// 4) Core에 계좌 등록 요청
		CoreAccountItemRes response =  coreAccountClient.createChildAccount(coreReq);

		// 5) JPA persist 필요하면 repository.save() 호출
		Account account = Account.builder()
			.user(child)
			.type(response.accountType())
			.accountNo(response.accountNumber())
			.build();
		log.info("자녀 용돈 통장 개설 완료: userId = {}", child.getId());
		accountRepository.save(account);
	}
}
