package dev.syntax.domain.card.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

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
import dev.syntax.domain.card.dto.CardInfoRes;
import dev.syntax.domain.card.entity.Card;
import dev.syntax.domain.card.repository.CardRepository;
import dev.syntax.domain.user.enums.Role;
import dev.syntax.global.auth.dto.UserContext;
import dev.syntax.global.exception.BusinessException;
import dev.syntax.global.response.error.ErrorBaseCode;

@ExtendWith(MockitoExtension.class)
class CardInquiryServiceTest {

    @InjectMocks
    private CardInquiryServiceImpl cardInquiryService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserContext userContext;

    @Nested
    @DisplayName("카드 조회 테스트")
    class GetCardInfo {

        @Test
        @DisplayName("성공: 자녀가 본인의 카드를 조회한다")
        void success_ChildViewsOwnCard() {
            // given
            Long childId = 10L;
            Account account = Account.builder().id(100L).build();
            Card card = Card.builder()
                    .id(500L)
                    .number("1234567812345678")
                    .name("KIM MINSEO")
                    .cvc("123")
                    .expiredAt("12/25")
                    .build();

            given(userContext.getId()).willReturn(childId);
            given(userContext.getRole()).willReturn(Role.CHILD.name());

            given(accountRepository.findByUserIdAndType(childId, AccountType.ALLOWANCE))
                    .willReturn(Optional.of(account));
            given(cardRepository.findByAccountId(account.getId())).willReturn(Optional.of(card));

            // when
            CardInfoRes res = cardInquiryService.getCardInfo(childId, userContext);

            // then
            assertThat(res.getCardId()).isEqualTo(500L);
            assertThat(res.getCardNumber()).isEqualTo("1234 5678 1234 5678"); // Formatting check
        }

        @Test
        @DisplayName("성공: 부모가 자녀의 카드를 조회한다")
        void success_ParentViewsChildCard() {
            // given
            Long childId = 10L;
            Account account = Account.builder().id(100L).build();
            Card card = Card.builder()
                    .id(500L)
                    .number("1234567812345678")
                    .name("KIM MINSEO")
                    .cvc("123")
                    .expiredAt("12/25")
                    .build();

            // given(userContext.getId()).willReturn(parentId); // Unnecessary
            given(userContext.getRole()).willReturn(Role.PARENT.name());
            given(userContext.getChildren()).willReturn(List.of(childId));

            given(accountRepository.findByUserIdAndType(childId, AccountType.ALLOWANCE))
                    .willReturn(Optional.of(account));
            given(cardRepository.findByAccountId(account.getId())).willReturn(Optional.of(card));

            // when
            CardInfoRes res = cardInquiryService.getCardInfo(childId, userContext);

            // then
            assertThat(res.getCardId()).isEqualTo(500L);
            assertThat(res.getCardNumber()).isEqualTo("1234 5678 1234 5678");
        }

        @Test
        @DisplayName("실패: 자녀가 다른 사람의 카드를 조회하면 UNAUTHORIZED 예외 발생")
        void fail_ChildViewsOtherCard() {
            // given
            Long childId = 10L;
            Long otherId = 20L;
            given(userContext.getId()).willReturn(childId);
            given(userContext.getRole()).willReturn(Role.CHILD.name());

            // when & then
            assertThatThrownBy(() -> cardInquiryService.getCardInfo(otherId, userContext))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorBaseCode.UNAUTHORIZED);
        }

        @Test
        @DisplayName("실패: 부모가 내 자녀가 아닌 아이의 카드를 조회하면 UNAUTHORIZED 예외 발생")
        void fail_ParentViewsOtherChildCard() {
            // given
            Long childId = 10L;
            Long otherChildId = 20L;

            // given(userContext.getId()).willReturn(parentId); // Unnecessary
            given(userContext.getRole()).willReturn(Role.PARENT.name());
            given(userContext.getChildren()).willReturn(List.of(childId)); // 다른 자녀만 있음

            // when & then
            assertThatThrownBy(() -> cardInquiryService.getCardInfo(otherChildId, userContext))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorBaseCode.UNAUTHORIZED);
        }

    }
}
