package de.exercise.billing;

import de.exercise.core.Money;
import de.exercise.core.Configuration;
import de.exercise.core.Rounding;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class InvoiceCalculator {

    static final int DUE_DAY = 15;

    private final BigDecimal taxRatePercent;

    public InvoiceCalculator(Configuration configuration) {
        this.taxRatePercent = configuration.taxRatePercent();
    }

    public Invoice calculate(Contract contract, YearMonth month) {
        Objects.requireNonNull(contract, "contract");
        Objects.requireNonNull(month, "month");
        checkTerm(contract, month);

        Tariff tariff = contract.tariff();
        Money net = tariff.monthlyFee();
        Money tax = net.percent(taxRatePercent, Rounding.HALF_UP);
        Money provisional = net.plus(tax);
        Money discount = provisional.percent(tariff.discountPercent(), Rounding.HALF_UP);
        Money gross = provisional.minus(discount);

        return new Invoice(contract, month, net, tax, discount, gross, dueDate(month));
    }

    private static void checkTerm(Contract contract, YearMonth month) {
        if (month.isBefore(YearMonth.from(contract.start()))) {
            throw new IllegalArgumentException(
                    "Vertrag %s beginnt erst am %s".formatted(contract.number(), contract.start()));
        }
        if (contract.end() != null && month.isAfter(YearMonth.from(contract.end()))) {
            throw new IllegalArgumentException(
                    "Vertrag %s endete am %s".formatted(contract.number(), contract.end()));
        }
    }

    private static LocalDate dueDate(YearMonth month) {
        return month.atDay(DUE_DAY);
    }
}
