package de.exercise.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.exercise.core.Money;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    private final Invoice invoice = new Invoice(
            TestData.basicContract(),
            YearMonth.of(2026, 9),
            Money.euro("20,00"),
            Money.euro("3,80"),
            Money.ZERO,
            Money.euro("23,80"),
            LocalDate.of(2026, 9, 15));

    @Test
    void asTextNamesMonthContractAndAmount() {
        assertEquals("Rechnung 2026-09 · V-1001 · 23,80 EUR", invoice.asText());
    }

    @Test
    void idConsistsOfContractAndMonth() {
        assertEquals("V-1001/2026-09", invoice.id());
    }
}
