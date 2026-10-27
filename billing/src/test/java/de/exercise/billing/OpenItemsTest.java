package de.exercise.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.exercise.core.Storage;
import de.exercise.core.Money;
import de.exercise.core.Configuration;
import de.exercise.core.InMemoryStorage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenItemsTest {

    private final Storage<Invoice> invoices = new InMemoryStorage<>();
    private final OpenItems items = new OpenItems(invoices);
    private Invoice august;
    private Invoice september;

    @BeforeEach
    void createInvoices() {
        InvoiceCalculator calculator = new InvoiceCalculator(Configuration.standard());
        august = calculator.calculate(TestData.basicContract(), YearMonth.of(2026, 8));
        september = calculator.calculate(TestData.basicContract(), YearMonth.of(2026, 9));
        invoices.save(august.id(), august);
        invoices.save(september.id(), september);
    }

    @Test
    void withoutPaymentFullAmountIsOpen() {
        assertEquals(Money.euro("23,80"), items.open(september));
        assertFalse(items.isPaid(september));
    }

    @Test
    void partialPaymentsAreSummed() {
        items.book(september.id(), Money.euro("10,00"));
        items.book(september.id(), Money.euro("5,80"));

        assertEquals(Money.euro("15,80"), items.paidAmount(september));
        assertEquals(Money.euro("8,00"), items.open(september));
    }

    @Test
    void overpaymentCountsAsPaid() {
        items.book(september.id(), Money.euro("30,00"));

        assertTrue(items.isPaid(september));
        assertEquals(Money.euro("-6,20"), items.open(september));
    }

    @Test
    void paymentOnUnknownInvoiceIsRejected() {
        assertThrows(NoSuchElementException.class, () -> items.book("V-1001/2030-01", Money.euro("1")));
    }

    @Test
    void paymentMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> items.book(september.id(), Money.ZERO));
        assertThrows(IllegalArgumentException.class, () -> items.book(september.id(), Money.euro("-1")));
    }

    @Test
    void overdueAreOnlyUnpaidInvoicesBeforeCutoff() {
        assertEquals(List.of(august), items.overdue(LocalDate.of(2026, 9, 15)));
        assertEquals(List.of(august, september), items.overdue(LocalDate.of(2026, 9, 16)));

        items.book(august.id(), Money.euro("23,80"));

        assertEquals(List.of(september), items.overdue(LocalDate.of(2026, 9, 16)));
    }

    @Test
    void sumOpenAcrossAllInvoices() {
        items.book(august.id(), Money.euro("20,00"));

        assertEquals(Money.euro("27,60"), items.sumOpen());
    }
}
