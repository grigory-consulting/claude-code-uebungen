package de.exercise.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.exercise.core.Money;
import de.exercise.core.Configuration;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class PaymentReminderTest {

    private final Configuration configuration = Configuration.standard();
    private final PaymentReminder paymentReminder = new PaymentReminder(configuration);
    private final Invoice invoice = new InvoiceCalculator(configuration)
            .calculate(TestData.basicContract(), YearMonth.of(2026, 9));

    @Test
    void noNoticeWithinDeadline() {
        assertTrue(paymentReminder.check(invoice, LocalDate.of(2026, 9, 29), false).isEmpty());
    }

    @Test
    void firstNoticeOnDayAfterDeadline() {
        ReminderNotice notice = paymentReminder.check(invoice, LocalDate.of(2026, 9, 30), false).orElseThrow();

        assertEquals(1, notice.level());
        assertEquals(Money.euro("5,00"), notice.fee());
        assertEquals(LocalDate.of(2026, 9, 30), notice.date());
        assertEquals(Money.euro("28,80"), notice.amountDue());
    }

    @Test
    void paidInvoiceGetsNoReminder() {
        assertTrue(paymentReminder.check(invoice, LocalDate.of(2026, 12, 1), true).isEmpty());
    }

    @Test
    void secondLevelAfterAnotherDeadline() {
        ReminderNotice notice = paymentReminder.check(invoice, LocalDate.of(2026, 10, 14), false).orElseThrow();

        assertEquals(2, notice.level());
        assertEquals(Money.euro("10,00"), notice.fee());
    }

    @Test
    void atMostThirdLevel() {
        ReminderNotice notice = paymentReminder.check(invoice, LocalDate.of(2027, 3, 1), false).orElseThrow();

        assertEquals(3, notice.level());
        assertEquals(Money.euro("15,00"), notice.fee());
    }
}
