package de.exercise.billing;

import de.exercise.core.Money;
import java.math.BigDecimal;
import java.util.Objects;

public record Tariff(String name, Money monthlyFee, BigDecimal discountPercent) {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public Tariff {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(monthlyFee, "monthlyFee");
        Objects.requireNonNull(discountPercent, "discountPercent");
        if (monthlyFee.isNegative()) {
            throw new IllegalArgumentException("Monatsbeitrag darf nicht negativ sein: " + monthlyFee);
        }
        if (discountPercent.signum() < 0 || discountPercent.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("Rabatt außerhalb 0 bis 100 Prozent: " + discountPercent);
        }
    }

    public static Tariff withoutDiscount(String name, Money monthlyFee) {
        return new Tariff(name, monthlyFee, BigDecimal.ZERO);
    }

    public boolean hasDiscount() {
        return discountPercent.signum() > 0;
    }
}
