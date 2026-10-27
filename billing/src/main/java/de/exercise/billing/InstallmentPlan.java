package de.exercise.billing;

import de.exercise.core.Money;
import de.exercise.core.Rounding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class InstallmentPlan {

    static final int MAX_INSTALLMENTS = 12;

    public List<Money> installments(Money total, int count) {
        Objects.requireNonNull(total, "total");
        if (count < 1 || count > MAX_INSTALLMENTS) {
            throw new IllegalArgumentException(
                    "Anzahl der Raten muss zwischen 1 und %d liegen: %d".formatted(MAX_INSTALLMENTS, count));
        }
        if (total.isNegative()) {
            throw new IllegalArgumentException("Ratenzahlung nur für positive Beträge: " + total);
        }
        Money installment = total.dividedBy(count, Rounding.HALF_UP);
        List<Money> installments = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            installments.add(installment);
        }
        return Collections.unmodifiableList(installments);
    }
}
