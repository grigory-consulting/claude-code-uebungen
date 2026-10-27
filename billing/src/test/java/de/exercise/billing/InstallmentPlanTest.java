package de.exercise.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.exercise.core.Money;
import java.util.List;
import org.junit.jupiter.api.Test;

class InstallmentPlanTest {

    private final InstallmentPlan installmentPlan = new InstallmentPlan();

    @Test
    void hundredEuroInFourInstallments() {
        List<Money> installments = installmentPlan.installments(Money.euro("100,00"), 4);

        assertEquals(List.of(
                Money.euro("25,00"), Money.euro("25,00"), Money.euro("25,00"), Money.euro("25,00")), installments);
    }

    @Test
    void installmentCountMustBeBetweenOneAndTwelve() {
        Money total = Money.euro("100,00");

        assertThrows(IllegalArgumentException.class, () -> installmentPlan.installments(total, 0));
        assertThrows(IllegalArgumentException.class, () -> installmentPlan.installments(total, 13));
    }
}
