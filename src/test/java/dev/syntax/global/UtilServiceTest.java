package dev.syntax.global;

import dev.syntax.global.service.Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * NumberFormattingService(int)의 동작을 검증하는 테스트.
 * <p>주의: String.format("%,d")는 시스템 Locale에 따라 구분자가 달라질 수 있어
 * 테스트에서는 Locale을 US로 고정해 쉼표(,)를 기대값으로 검증합니다.</p>
 */
@ActiveProfiles("test")
public class UtilServiceTest {

    private static Locale original;

    // 세자수마다 쉼표를 붙이도록 US기준 설정
    @BeforeAll
    static void setUpLocale() {
        original = Locale.getDefault();
        Locale.setDefault(Locale.US); // 천 단위 구분자: ','
    }

    @AfterAll
    static void restoreLocale() {
        Locale.setDefault(original);
    }

    // 세자리수 미만의 수가 주어질 때
    @Test
    void format_under_three_digits_returns_same_string() {
        assertEquals("0", Utils.NumberFormattingService(0));
        assertEquals("5", Utils.NumberFormattingService(5));
        assertEquals("12", Utils.NumberFormattingService(12));
        assertEquals("999", Utils.NumberFormattingService(999));
    }

    // 3의 배수 자리수 수가 주어질 때 (예: 6자리, 9자리 등)
    @Test
    void format_length_multiple_of_three_has_commas_every_three() {
        assertEquals("123,456", Utils.NumberFormattingService(123456));
        assertEquals("1,000,000", Utils.NumberFormattingService(1000000));
        assertEquals("123,456,789", Utils.NumberFormattingService(123456789));
    }

    // 3의 배수 자리수가 아닌 수가 주어질 때 (예: 4자리, 5자리, 7자리 …)
    @Test
    void format_length_not_multiple_of_three_places_commas_correctly() {
        assertEquals("1,234", Utils.NumberFormattingService(1234));
        assertEquals("12,345", Utils.NumberFormattingService(12345));
        assertEquals("1,234,567", Utils.NumberFormattingService(1234567));
    }

    // 0이 주어질 때
    @Test
    void format_zero_is_zero_string() {
        assertEquals("0", Utils.NumberFormattingService(0));
    }
}