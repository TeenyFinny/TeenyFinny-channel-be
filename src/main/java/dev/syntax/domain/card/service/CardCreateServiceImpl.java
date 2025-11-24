package dev.syntax.domain.card.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.account.enums.AccountType;
import dev.syntax.domain.account.repository.AccountRepository;
import dev.syntax.domain.card.dto.CardCreateReq;
import dev.syntax.domain.card.dto.CardInfoRes;
import dev.syntax.domain.card.entity.Card;
import dev.syntax.domain.card.factory.CardFactory;
import dev.syntax.domain.card.repository.CardRepository;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;

@Service
@RequiredArgsConstructor
@Transactional
public class CardCreateServiceImpl implements CardCreateService {

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardFactory cardFactory;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public CardInfoRes createCard(CardCreateReq req, UserContext ctx) {

        Long childId = req.getChildId();

        //  1. 부모 권한 검증
        validateAccess(ctx, childId);

        // 2. 용돈 계좌(ALLOWANCE) 존재 확인
        Account account = accountRepository
                .findByUserIdAndType(childId, AccountType.ALLOWANCE)
                .orElseThrow(() -> new BusinessException(ErrorBaseCode.ACCOUNT_NOT_FOUND));

        //  3. 이미 카드가 존재하면 에러
        if (cardRepository.existsByAccountId(account.getId())) {
            throw new BusinessException(ErrorBaseCode.CARD_ALREADY_EXISTS);
        }

        // 🔢 4. 카드 자동 생성 (Factory 사용)
        Card card = cardFactory.create(account, req);
        cardRepository.save(card);

        return new CardInfoRes(card.getId(), card.getNumber(), card.getCvc(), card.getExpiredAt());
    }

    private void validateAccess(UserContext ctx, Long childId) {
        // 자녀 본인은 카드 생성 불가 (부모만 가능)
        if (ctx.getId().equals(childId)) {
            throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
        }

        // 부모이고, 해당 자녀가 내 자녀 목록에 있어야 함
        if (ctx.getRole().equals(Role.PARENT.name()) &&
                ctx.getChildren().contains(childId)) {
            return; // 부모 OK
        }

        throw new BusinessException(ErrorBaseCode.UNAUTHORIZED);
    }
}
