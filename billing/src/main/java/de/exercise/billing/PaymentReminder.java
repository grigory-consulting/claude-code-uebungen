package de.exercise.billing;

import de.exercise.core.Money;
import de.exercise.core.Configuration;
import de.exercise.core.Rounding;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

public class PaymentReminder {

    static final int HIGHEST_LEVEL = 3;

    private final Money reminderFee;
    private final int reminderPeriodDays;

    public PaymentReminder(Configuration configuration) {
        this.reminderFee = configuration.reminderFee();
        this.reminderPeriodDays = configuration.reminderPeriodDays();
    }

    public Optional<ReminderNotice> check(Invoice invoice, LocalDate today, boolean paid) {
        Objects.requireNonNull(invoice, "invoice");
        Objects.requireNonNull(today, "today");
        if (paid) {
            return Optional.empty();
        }
        LocalDate deadline = invoice.due().plusDays(reminderPeriodDays);
        if (!today.isAfter(deadline)) {
            return Optional.empty();
        }
        int level = level(deadline, today);
        Money fee = reminderFee.times(BigDecimal.valueOf(level), Rounding.HALF_UP);
        return Optional.of(new ReminderNotice(invoice, level, fee, today));
    }

    private int level(LocalDate deadline, LocalDate today) {
        long daysOverdue = ChronoUnit.DAYS.between(deadline, today);
        long level = 1 + (daysOverdue - 1) / reminderPeriodDays;
        return (int) Math.min(level, HIGHEST_LEVEL);
    }
}
