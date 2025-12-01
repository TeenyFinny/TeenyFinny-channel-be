package dev.syntax.domain.card.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.card.dto.CardCreateReq;
import dev.syntax.domain.card.entity.Card;
import dev.syntax.domain.card.repository.CardRepository;

@ExtendWith(MockitoExtension.class)
class CardFactoryTest {

    @InjectMocks
    private CardFactory cardFactory;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CardCreateReq req;

    @Test
    @DisplayName("카드 생성 성공: 유니크한 번호와 암호화된 비밀번호가 설정된다")
    void createCard_Success() {
        // given
        Account account = Account.builder().id(1L).build();
        given(req.getPassword()).willReturn("1234");
        given(req.getEnglishName()).willReturn("KIM MINSEO");

        given(passwordEncoder.encode("1234")).willReturn("encoded_password");
        given(cardRepository.existsByNumber(anyString())).willReturn(false); // 중복 없음

        // when
        Card card = cardFactory.create(account, req);

        // then
        assertThat(card.getAccount()).isEqualTo(account);
        assertThat(card.getNumber()).startsWith("4"); // VISA style
        assertThat(card.getNumber()).hasSize(16);
        assertThat(card.getCvc()).hasSize(3);
        assertThat(card.getExpiredAt()).hasSize(5); // MM/yy
        assertThat(card.getPassword()).isEqualTo("encoded_password");
        assertThat(card.getName()).isEqualTo("KIM MINSEO");
    }

    @Test
    @DisplayName("카드 번호 중복 시 재시도하여 유니크한 번호를 생성한다")
    void createCard_RetryOnDuplicateNumber() {
        // given
        Account account = Account.builder().id(1L).build();
        given(req.getPassword()).willReturn("1234");
        given(passwordEncoder.encode("1234")).willReturn("encoded_password");

        // 첫 번째 생성된 번호는 중복(true), 두 번째는 중복 아님(false)
        given(cardRepository.existsByNumber(anyString()))
                .willReturn(true)
                .willReturn(false);

        // when
        Card card = cardFactory.create(account, req);

        // then
        assertThat(card).isNotNull();
        assertThat(card.getNumber()).hasSize(16);
    }
}
