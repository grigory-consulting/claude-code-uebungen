package de.exercise.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.exercise.core.Money;
import de.exercise.core.Configuration;
import de.exercise.core.InMemoryStorage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContractServiceTest {

    private static final YearMonth SEPTEMBER = YearMonth.of(2026, 9);

    private final ByteArrayOutputStream log = new ByteArrayOutputStream();
    private PrintStream stderrBefore;
    private ContractService service;

    @BeforeEach
    void setUp() {
        stderrBefore = System.err;
        System.setErr(new PrintStream(log, true, StandardCharsets.UTF_8));
        Configuration configuration = Configuration.standard();
        service = new ContractService(new InMemoryStorage<>(), new InMemoryStorage<>(),
                new InvoiceCalculator(configuration), new PaymentReminder(configuration));
        service.create(TestData.basicContract());
        service.create(TestData.comfortContract());
    }

    @AfterEach
    void cleanUp() {
        System.setErr(stderrBefore);
    }

    @Test
    void contractCanBeCreatedOnlyOnce() {
        assertThrows(IllegalStateException.class, () -> service.create(TestData.basicContract()));
    }

    @Test
    void unknownContractIsReported() {
        assertThrows(NoSuchElementException.class, () -> service.bill("V-9999", SEPTEMBER));
    }

    @Test
    void secondBillingReturnsSavedInvoice() {
        Invoice first = service.bill("V-1001", SEPTEMBER);
        Invoice second = service.bill("V-1001", SEPTEMBER);

        assertSame(first, second);
        assertEquals(1, service.invoicesFor("V-1001").size());
    }

    @Test
    void monthlyRunBillsOnlyActiveContracts() {
        service.create(TestData.contract("V-1005", TestData.PREMIUM, LocalDate.of(2026, 11, 1)));

        List<Invoice> invoices = service.monthlyRun(SEPTEMBER);

        assertEquals(2, invoices.size());
        assertEquals(Money.euro("64,16"), ContractService.sum(invoices));
    }

    @Test
    void tariffChangeAffectsNextInvoice() {
        service.bill("V-1001", SEPTEMBER);
        service.changeTariff("V-1001", TestData.COMFORT);

        Invoice october = service.bill("V-1001", SEPTEMBER.plusMonths(1));

        assertEquals(Money.euro("40,36"), october.gross());
        assertEquals(Money.euro("23,80"), service.invoicesFor("V-1001").get(0).gross());
    }

    @Test
    void noticeForUnpaidInvoice() {
        service.bill("V-1002", SEPTEMBER);

        assertTrue(service.checkReminder("V-1002", SEPTEMBER, LocalDate.of(2026, 10, 1), false).isPresent());
        assertFalse(service.checkReminder("V-1002", SEPTEMBER, LocalDate.of(2026, 10, 1), true).isPresent());
    }

    @Test
    void reminderRunOnlyCoversUnpaidOverdueInvoices() {
        InMemoryStorage<Invoice> storage = new InMemoryStorage<>();
        Configuration configuration = Configuration.standard();
        ContractService withStorage = new ContractService(new InMemoryStorage<>(), storage,
                new InvoiceCalculator(configuration), new PaymentReminder(configuration));
        withStorage.create(TestData.basicContract());
        withStorage.create(TestData.comfortContract());
        withStorage.monthlyRun(SEPTEMBER);
        OpenItems items = new OpenItems(storage);
        items.book("V-1001/2026-09", Money.euro("23,80"));

        List<ReminderNotice> notices = withStorage.reminderRun(items, LocalDate.of(2026, 10, 2));

        assertEquals(1, notices.size());
        assertEquals("V-1002", notices.get(0).invoice().contract().number());
    }

    @Test
    void installmentPlanForInvoiceAmount() {
        List<Money> installments = service.installmentPlan("V-1001", SEPTEMBER, 2);

        assertEquals(List.of(Money.euro("11,90"), Money.euro("11,90")), installments);
    }

    @Test
    void logContainsNoTenantNames() {
        service.monthlyRun(SEPTEMBER);
        service.checkReminder("V-1001", SEPTEMBER, LocalDate.of(2026, 11, 1), false);

        String text = log.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("V-1001"));
        assertFalse(text.contains(TestData.NORDTAL.name()));
    }
}
