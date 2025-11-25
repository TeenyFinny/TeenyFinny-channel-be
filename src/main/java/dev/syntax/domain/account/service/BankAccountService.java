package dev.syntax.domain.account.service;

import dev.syntax.domain.user.dto.CoreParentInitRes;
import dev.syntax.domain.user.entity.User;

public interface BankAccountService {
	void creatParentAccount(User user, CoreParentInitRes res);
}
