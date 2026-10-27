package de.exercise.billing;

import de.exercise.core.Money;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

/**
 * Rechnungen als CSV für die Buchhaltung, Trennzeichen Semikolon, Beträge mit Dezimalkomma.
 */
public class InvoiceExport {

    static final String HEADER = "rechnung;vertrag;mandant;monat;netto;steuer;rabatt;brutto;faellig";

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String asCsv(List<Invoice> invoices) {
        StringJoiner lines = new StringJoiner("\n", "", "\n");
        lines.add(HEADER);
        for (Invoice invoice : invoices) {
            lines.add(line(invoice));
        }
        return lines.toString();
    }

    String line(Invoice invoice) {
        return String.join(";",
                invoice.id(),
                invoice.contract().number(),
                invoice.contract().tenant().identifier(),
                invoice.month().toString(),
                decimal(invoice.net()),
                decimal(invoice.tax()),
                decimal(invoice.discount()),
                decimal(invoice.gross()),
                invoice.due().format(DATE));
    }

    static String decimal(Money amount) {
        long cents = Math.abs(amount.inCents());
        return "%s%d,%02d".formatted(amount.isNegative() ? "-" : "", cents / 100, cents % 100);
    }
}
