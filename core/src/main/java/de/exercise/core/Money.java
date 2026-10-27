package de.exercise.core;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Geldbetrag in Euro, intern in Cent gespeichert. Unveränderlich.
 */
public final class Money implements Comparable<Money> {

    public static final Money ZERO = new Money(0);

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final long cents;

    private Money(long cents) {
        this.cents = cents;
    }

    public static Money cents(long cents) {
        return cents == 0 ? ZERO : new Money(cents);
    }

    /**
     * Liest einen Eurobetrag wie {@code "12,34"}, {@code "12.34"} oder {@code "-5"}.
     */
    public static Money euro(String text) {
        Objects.requireNonNull(text, "text");
        String cleaned = text.trim().replace(',', '.');
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Leerer Betrag");
        }
        BigDecimal value;
        try {
            value = new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Kein gültiger Betrag: " + text, e);
        }
        if (value.scale() > 2) {
            throw new IllegalArgumentException("Mehr als zwei Nachkommastellen: " + text);
        }
        return cents(value.movePointRight(2).longValueExact());
    }

    public Money plus(Money other) {
        return cents(Math.addExact(cents, other.cents));
    }

    public Money minus(Money other) {
        return cents(Math.subtractExact(cents, other.cents));
    }

    public Money times(BigDecimal factor, Rounding rounding) {
        BigDecimal result = BigDecimal.valueOf(cents).multiply(factor);
        return cents(result.setScale(0, rounding.mode()).longValueExact());
    }

    public Money percent(BigDecimal percent, Rounding rounding) {
        BigDecimal result = BigDecimal.valueOf(cents).multiply(percent).divide(HUNDRED);
        return cents(result.setScale(0, rounding.mode()).longValueExact());
    }

    public Money dividedBy(int divisor, Rounding rounding) {
        if (divisor <= 0) {
            throw new IllegalArgumentException("Teiler muss positiv sein: " + divisor);
        }
        BigDecimal result = BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(divisor), 0, rounding.mode());
        return cents(result.longValueExact());
    }

    public boolean isNegative() {
        return cents < 0;
    }

    public long inCents() {
        return cents;
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(cents, other.cents);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money other && cents == other.cents;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(cents);
    }

    @Override
    public String toString() {
        long amount = Math.abs(cents);
        String sign = cents < 0 ? "-" : "";
        return "%s%d,%02d EUR".formatted(sign, amount / 100, amount % 100);
    }
}
