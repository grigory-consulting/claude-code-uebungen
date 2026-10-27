package de.exercise.billing;

import de.exercise.core.Storage;
import de.exercise.core.Money;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Zahlungseingänge je Rechnung und der daraus offene Betrag.
 */
public class OpenItems {

    private final Storage<Invoice> invoices;
    private final Map<String, Money> paidAmount = new HashMap<>();

    public OpenItems(Storage<Invoice> invoices) {
        this.invoices = invoices;
    }

    public void book(String invoiceId, Money payment) {
        if (invoices.load(invoiceId).isEmpty()) {
            throw new NoSuchElementException("Unbekannte Rechnung: " + invoiceId);
        }
        if (payment.isNegative() || payment.equals(Money.ZERO)) {
            throw new IllegalArgumentException("Zahlung muss positiv sein: " + payment);
        }
        paidAmount.merge(invoiceId, payment, Money::plus);
    }

    public Money paidAmount(Invoice invoice) {
        return paidAmount.getOrDefault(invoice.id(), Money.ZERO);
    }

    public Money open(Invoice invoice) {
        return invoice.gross().minus(paidAmount(invoice));
    }

    public boolean isPaid(Invoice invoice) {
        return open(invoice).compareTo(Money.ZERO) <= 0;
    }

    public List<Invoice> overdue(LocalDate cutoffDate) {
        return invoices.all().stream()
                .filter(r -> r.due().isBefore(cutoffDate))
                .filter(r -> !isPaid(r))
                .sorted(Comparator.comparing(Invoice::due).thenComparing(Invoice::id))
                .toList();
    }

    public Money sumOpen() {
        Money sum = Money.ZERO;
        for (Invoice invoice : invoices.all()) {
            if (!isPaid(invoice)) {
                sum = sum.plus(open(invoice));
            }
        }
        return sum;
    }
}
