package de.exercise.billing;

import de.exercise.core.Money;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public record Invoice(
        Contract contract,
        YearMonth month,
        Money net,
        Money tax,
        Money discount,
        Money gross,
        LocalDate due) {

    public Invoice {
        Objects.requireNonNull(contract, "contract");
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(net, "net");
        Objects.requireNonNull(tax, "tax");
        Objects.requireNonNull(discount, "discount");
        Objects.requireNonNull(gross, "gross");
        Objects.requireNonNull(due, "due");
    }

    public String id() {
        return contract.number() + "/" + month;
    }

    public String asText() {
        return "Rechnung %s · %s · %s".formatted(month, contract.number(), gross);
    }
}
