package de.exercise.billing;

import de.exercise.core.Money;
import java.time.LocalDate;

public record ReminderNotice(Invoice invoice, int level, Money fee, LocalDate date) {

    public Money amountDue() {
        return invoice.gross().plus(fee);
    }
}
