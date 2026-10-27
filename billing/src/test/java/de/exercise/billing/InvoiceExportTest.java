package de.exercise.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.exercise.core.Money;
import de.exercise.core.Configuration;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceExportTest {

    private final InvoiceCalculator calculator = new InvoiceCalculator(Configuration.standard());
    private final InvoiceExport export = new InvoiceExport();

    @Test
    void oneLinePerInvoiceWithHeader() {
        Invoice invoice = calculator.calculate(TestData.comfortContract(), YearMonth.of(2026, 9));

        String csv = export.asCsv(List.of(invoice));

        assertEquals(InvoiceExport.HEADER + "\n"
                + "V-1002/2026-09;V-1002;M-01;2026-09;39,90;7,58;7,12;40,36;15.09.2026\n", csv);
    }

    @Test
    void emptyListGivesOnlyHeader() {
        assertEquals(InvoiceExport.HEADER + "\n", export.asCsv(List.of()));
    }

    @Test
    void exportContainsNoTenantNames() {
        Invoice invoice = calculator.calculate(TestData.basicContract(), YearMonth.of(2026, 9));

        String line = export.line(invoice);

        assertEquals(-1, line.indexOf(TestData.NORDTAL.name()));
    }

    @Test
    void decimalWithCommaAndSign() {
        assertEquals("0,07", InvoiceExport.decimal(Money.cents(7)));
        assertEquals("-12,50", InvoiceExport.decimal(Money.euro("-12,5")));
        assertEquals("1500,00", InvoiceExport.decimal(Money.euro("1500")));
    }
}
