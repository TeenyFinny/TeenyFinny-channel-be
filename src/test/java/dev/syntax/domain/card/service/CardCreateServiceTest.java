package dev.syntax.domain.card.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class CardCreateServiceTest {

    @InjectMocks
    private CardCreateServiceImpl cardCreateService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardFactory cardFactory;

    @Mock
    private UserContext userContext;

    @Mock
    private CardCreateReq req;

    @Nested
    @DisplayName("카드 생성 테스트")
    class CreateCard {

        @Test
        @DisplayName("성공: 부모가 자녀의 카드를 생성한다")
        void success_ParentCreatesChildCard() {
            // given
            Long childId = 10L;
            Long parentId = 1L;
            Account account = Account.builder().id(100L).build();
            Card card = Card.builder()
                    .id(500L)
                    .number("1234567812345678")
                    .name("KIM MINSEO")
                    .cvc("123")
                    .expiredAt("12/25")
                    .build();

            given(req.getChildId()).willReturn(childId);
            given(userContext.getId()).willReturn(parentId);
            given(userContext.getRole()).willReturn(Role.PARENT.name());
            given(userContext.getChildren()).willReturn(List.of(childId));

            given(accountRepository.findByUserIdAndType(childId, AccountType.ALLOWANCE))
                    .willReturn(Optional.of(account));
            given(cardRepository.existsByAccountId(account.getId())).willReturn(false);
            given(cardFactory.create(account, req)).willReturn(card);

            // when
            CardInfoRes res = cardCreateService.createCard(req, userContext);

            // then
            assertThat(res.getCardId()).isEqualTo(500L);
            assertThat(res.getCardNumber()).isEqualTo("1234 5678 1234 5678"); // Formatting check
            verify(cardRepository).save(card);
        }

        @Test
        @DisplayName("실패: 자녀가 본인의 카드를 생성하려 하면 UNAUTHORIZED 예외 발생")
        void fail_ChildCreatesOwnCard() {
            // given
            Long childId = 10L;
            given(req.getChildId()).willReturn(childId);
            given(userContext.getId()).willReturn(childId); // 본인 ID

            // when & then
            assertThatThrownBy(() -> cardCreateService.createCard(req, userContext))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorBaseCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("실패: 부모가 자신의 자녀가 아닌 아이의 카드를 생성하려 하면 UNAUTHORIZED 예외 발생")
        void fail_ParentCreatesOtherChildCard() {
            // given
            Long childId = 10L;
            Long parentId = 1L;
            Long otherChildId = 20L;

            given(req.getChildId()).willReturn(otherChildId);
            given(userContext.getId()).willReturn(parentId);
            given(userContext.getRole()).willReturn(Role.PARENT.name());
            given(userContext.getChildren()).willReturn(List.of(childId)); // 다른 자녀만 있음

            // when & then
            assertThatThrownBy(() -> cardCreateService.createCard(req, userContext))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorBaseCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("실패: 용돈 계좌가 없으면 ACCOUNT_NOT_FOUND 예외 발생")
        void fail_AccountNotFound() {
            // given
            Long childId = 10L;
            Long parentId = 1L;

            given(req.getChildId()).willReturn(childId);
            given(userContext.getId()).willReturn(parentId);
            given(userContext.getRole()).willReturn(Role.PARENT.name());
            given(userContext.getChildren()).willReturn(List.of(childId));

            given(accountRepository.findByUserIdAndType(childId, AccountType.ALLOWANCE))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cardCreateService.createCard(req, userContext))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorBaseCode.ACCOUNT_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 이미 카드가 존재하면 CARD_ALREADY_EXISTS 예외 발생")
        void fail_CardAlreadyExists() {
            // given
            Long childId = 10L;
            Long parentId = 1L;
            Account account = Account.builder().id(100L).build();

            given(req.getChildId()).willReturn(childId);
            given(userContext.getId()).willReturn(parentId);
            given(userContext.getRole()).willReturn(Role.PARENT.name());
            given(userContext.getChildren()).willReturn(List.of(childId));

            given(accountRepository.findByUserIdAndType(childId, AccountType.ALLOWANCE))
                    .willReturn(Optional.of(account));
            given(cardRepository.existsByAccountId(account.getId())).willReturn(true); // 이미 존재

            // when & then
            assertThatThrownBy(() -> cardCreateService.createCard(req, userContext))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorBaseCode.CARD_ALREADY_EXISTS);
        }
    }
}
