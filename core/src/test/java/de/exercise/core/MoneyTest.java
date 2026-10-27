package de.exercise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "12,34 | 1234",
            "12.34 | 1234",
            "-5    | -500",
            "0,5   | 50",
            "1000  | 100000"
    })
    void euroReadsCommaAndDot(String text, long expectedCents) {
        assertEquals(expectedCents, Money.euro(text).inCents());
    }

    @Test
    void euroRejectsThreeDecimals() {
        assertThrows(IllegalArgumentException.class, () -> Money.euro("1,005"));
    }

    @Test
    void euroRejectsTextWithoutNumber() {
        assertThrows(IllegalArgumentException.class, () -> Money.euro("zwölf"));
    }

    @Test
    void zeroCentsReturnsConstant() {
        assertSame(Money.ZERO, Money.cents(0));
    }

    @Test
    void plusAndMinusCalculateInCents() {
        Money a = Money.euro("10,10");
        Money b = Money.euro("0,95");
        assertEquals(Money.euro("11,05"), a.plus(b));
        assertEquals(Money.euro("9,15"), a.minus(b));
    }

    @Test
    void timesRoundsHalfUp() {
        Money amount = Money.euro("10,05");
        assertEquals(Money.euro("5,03"), amount.times(new BigDecimal("0.5"), Rounding.HALF_UP));
        assertEquals(Money.euro("5,02"), amount.times(new BigDecimal("0.5"), Rounding.DOWN));
    }

    @Test
    void percentOfFortyNineNinety() {
        Money amount = Money.euro("49,90");
        assertEquals(Money.euro("9,48"), amount.percent(new BigDecimal("19"), Rounding.HALF_UP));
        assertEquals(Money.euro("9,48"), amount.percent(new BigDecimal("19"), Rounding.DOWN));
        assertEquals(Money.euro("4,99"), amount.percent(new BigDecimal("10"), Rounding.HALF_UP));
    }

    @Test
    void percentWithHalfCentRoundsUp() {
        Money amount = Money.euro("0,50");
        assertEquals(Money.cents(1), amount.percent(new BigDecimal("1"), Rounding.HALF_UP));
        assertEquals(Money.ZERO, amount.percent(new BigDecimal("1"), Rounding.DOWN));
    }

    @Test
    void dividedByFourIsExact() {
        assertEquals(Money.euro("25,00"), Money.euro("100,00").dividedBy(4, Rounding.HALF_UP));
    }

    @Test
    void dividedWithRemainderRoundsAsGiven() {
        Money amount = Money.euro("10,00");
        assertEquals(Money.euro("1,43"), amount.dividedBy(7, Rounding.HALF_UP));
        assertEquals(Money.euro("1,42"), amount.dividedBy(7, Rounding.DOWN));
    }

    @Test
    void divisionByZeroIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> Money.euro("1,00").dividedBy(0, Rounding.HALF_UP));
    }

    @Test
    void negativeAmountsAreDetected() {
        assertTrue(Money.euro("-0,01").isNegative());
        assertFalse(Money.ZERO.isNegative());
    }

    @Test
    void textWithTwoDecimalsAndCurrency() {
        assertEquals("23,80 EUR", Money.euro("23,8").toString());
        assertEquals("-0,05 EUR", Money.cents(-5).toString());
        assertEquals("1234,00 EUR", Money.euro("1234").toString());
    }

    @Test
    void equalCentsAreEqualAmounts() {
        assertEquals(Money.euro("7,00"), Money.cents(700));
        assertEquals(Money.euro("7,00").hashCode(), Money.cents(700).hashCode());
    }

    @Test
    void sortedByValue() {
        List<Money> amounts = new ArrayList<>(List.of(Money.euro("3"), Money.euro("-1"), Money.euro("2")));
        Collections.sort(amounts);
        assertEquals(List.of(Money.euro("-1"), Money.euro("2"), Money.euro("3")), amounts);
    }
}
