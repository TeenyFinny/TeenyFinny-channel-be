package dev.syntax.domain.account.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.user.dto.CoreParentInitRes;
import dev.syntax.domain.user.entity.User;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

	private final AccountRepository accountRepository;

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
}
