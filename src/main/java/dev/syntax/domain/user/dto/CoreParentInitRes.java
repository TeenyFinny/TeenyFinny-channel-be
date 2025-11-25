package dev.syntax.domain.user.dto;

import dev.syntax.domain.account.dto.core.CoreAccountItemRes;

public record CoreParentInitRes(Long coreUserId,
								CoreAccountItemRes account
) implements CoreInitRes {
}
