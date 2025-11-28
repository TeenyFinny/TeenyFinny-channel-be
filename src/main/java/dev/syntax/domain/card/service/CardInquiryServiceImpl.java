package dev.syntax.domain.card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.card.dto.CardInfoRes;
import dev.syntax.domain.card.entity.Card;
import dev.syntax.domain.card.repository.CardRepository;
import dev.syntax.domain.card.util.CardUtils;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;

/**
 * 카드 조회 서비스 구현체.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardInquiryServiceImpl implements CardInquiryService {

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;

    /**
     * 카드 정보 조회.
     * <p>
     * 1. 권한 검증 (자녀 본인 or 부모)
     * 2. 용돈 계좌(ALLOWANCE) 조회
     * 3. 해당 계좌에 연결된 카드 조회
     * </p>
     */
    @Override
    public CardInfoRes getCardInfo(Long targetUserId, UserContext ctx) {

        //  1. 권한 검증
        validateAccess(targetUserId, ctx);

        //  2. 용돈 계좌 조회
        Account account = accountRepository
                .findByUserIdAndType(targetUserId, AccountType.ALLOWANCE)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        //  3. 카드 조회
        Card card = cardRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.CARD_NOT_FOUND));

        //  4. DTO 변환
        return new CardInfoRes(
                card.getId(),
                CardUtils.formatCardNumber(card.getNumber()),
                card.getName(),
                card.getCvc(),
                card.getExpiredAt()
        );
    }

    private void validateAccess(Long targetUserId, UserContext ctx) {

        // 자녀 본인 조회 허용
        if (ctx.getRole().equals(Role.CHILD.name()) && ctx.getId().equals(targetUserId)) {
            return;
        }

        // 부모가 자신의 자녀 카드 조회 허용
        if (ctx.getRole().equals(Role.PARENT.name()) && ctx.getChildren().contains(targetUserId)) {
            return;
        }

        throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
    }
}
