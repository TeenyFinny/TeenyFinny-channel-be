package dev.syntax.domain.card.factory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import dev.syntax.domain.account.entity.Account;
import dev.syntax.domain.card.dto.CardCreateReq;
import dev.syntax.domain.card.entity.Card;
import dev.syntax.domain.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class CardFactory {

    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;

    public Card create(Account account, CardCreateReq req) {

        return Card.builder()
                .account(account)
                .number(generateUniqueCardNumber())
                .cvc(generateCVC())
                .expiredAt(generateExpiredAt())
                .password(passwordEncoder.encode(req.getPassword()))
                .name(null)  // 초기에는 이름 없음
                .build();
    }

    private String generateUniqueCardNumber() {
        String number;
        do {
            number = generateCardNumber();
        } while (cardRepository.existsByNumber(number));
        return number;
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder("4");
        while (sb.length() < 16) {
            sb.append((int)(Math.random() * 10));
        }
        return sb.toString();
    }

    private String generateCVC() {
        int cvc = (int)(Math.random() * 900) + 100;
        return String.valueOf(cvc);
    }

    private String generateExpiredAt() {
        LocalDate date = LocalDate.now().plusYears(5);
        return date.format(DateTimeFormatter.ofPattern("MM/yy"));
    }
}

