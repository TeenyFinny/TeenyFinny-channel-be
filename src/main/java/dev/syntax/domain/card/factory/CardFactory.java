package dev.syntax.domain.card.factory;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.card.dto.CardCreateReq;
import dev.syntax.domain.card.entity.Card;
import dev.syntax.domain.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 카드 생성 로직을 담당하는 Factory 클래스.
 * <p>
 * 카드 번호, CVC, 유효기간 등 카드 발급에 필요한 데이터를 생성하고
 * Card 엔티티를 조립합니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CardFactory {

    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 새로운 카드를 생성합니다.
     *
     * @param account 연결할 계좌 엔티티
     * @param req     카드 생성 요청 정보 (비밀번호, 영문명 등)
     * @return 생성된 Card 엔티티 (저장되지 않은 상태)
     */
    public Card create(Account account, CardCreateReq req) {

        return Card.builder()
                .account(account)
                .number(generateUniqueCardNumber())
                .cvc(generateCVC())
                .expiredAt(generateExpiredAt())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(req.getEnglishName())  // 영문 이름 표기
                .build();
    }

    /**
     * 중복되지 않는 유니크한 카드 번호를 생성합니다.
     */
    private String generateUniqueCardNumber() {
        final int MAX_RETRIES = 10;

        for (int i = 0; i < MAX_RETRIES; i++) {
            String number = generateCardNumber();
            if (!cardRepository.existsByNumber(number)) {
                return number;
            }
        }

        throw new IllegalStateException(
                "최대 시도 횟수(" + MAX_RETRIES + "회) 후에도 고유한 카드 번호를 생성하는 데 실패했습니다."
        );
    }

    /**
     * 16자리 카드 번호 랜덤 생성 (VISA 스타일: 4로 시작).
     */
    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder("4");
        while (sb.length() < 16) {
            sb.append(secureRandom.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 3자리 CVC 번호 랜덤 생성 (100 ~ 999).
     */
    private String generateCVC() {
        int cvc = secureRandom.nextInt(900) + 100;
        return String.valueOf(cvc);
    }

    /**
     * 유효기간 생성 (현재로부터 5년 후, MM/yy 포맷).
     */
    private String generateExpiredAt() {
        LocalDate date = LocalDate.now().plusYears(5);
        return date.format(DateTimeFormatter.ofPattern("MM/yy"));
    }
}

