package dev.syntax.domain.account.service;

import dev.syntax.domain.account.client.CoreAccountClient;
import dev.syntax.domain.account.dto.CreateChildAccountReq;
import dev.syntax.domain.account.dto.core.CoreAccountItemRes;
import dev.syntax.domain.account.dto.core.CoreCreateAccountReq;
import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
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

import java.util.Optional;

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
	public void createParentAccount(User user, CoreParentInitRes res) {

		if (res.account() == null) {
			throw new BusinessException(ErrorBaseCode.INVALID_REQUEST);
		}

		if (res.account().accountType() == null) {
			throw new BusinessException(ErrorBaseCode.INVALID_ACCOUNT_TYPE);
		}

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

		if (req == null) {
			throw new BusinessException(ErrorBaseCode.INVALID_REQUEST);
		}

		User child = userRepository.findById(req.childId())
				.orElseThrow(() -> new BusinessException(ErrorBaseCode.USER_NOT_FOUND));


		// 이미 개설된 용돈 계좌 존재 여부 확인
		Optional<Account> isExists = accountRepository
				.findByUserIdAndType(child.getId(), AccountType.ALLOWANCE);
		if (isExists.isPresent()) {
			log.error("이미 해당 자녀에게 용돈 계좌가 존재합니다. userId={}, type={}", 
					child.getId(), AccountType.ALLOWANCE);
			throw new BusinessException(ErrorBaseCode.ACCOUNT_ALREADY_EXISTS);
		}

		boolean isChildOfParent = parent.getChildren().stream()
				.anyMatch(relationship -> relationship.getChild().getId().equals(child.getId()));
		if (!isChildOfParent) {
			throw new BusinessException(ErrorBaseCode.INVALID_CHILD);
		}

		CoreCreateAccountReq coreReq = CoreCreateAccountReq.builder()
				.parentCoreId(parent.getCoreUserId())
				.childCoreId(child.getCoreUserId())
				.build();

		CoreAccountItemRes response =  coreAccountClient.createChildAccount(coreReq);

		Account account = Account.builder()
			.user(child)
			.type(response.accountType())
			.accountNo(response.accountNumber())
			.build();
		log.info("자녀 용돈 통장 개설 완료: userId={}", child.getId());
		accountRepository.save(account);
	}
}
