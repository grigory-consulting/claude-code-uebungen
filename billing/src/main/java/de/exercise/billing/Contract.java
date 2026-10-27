package de.exercise.billing;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Vertrag eines Mandanten. {@code end == null} bedeutet unbefristet.
 */
public record Contract(String number, Tenant tenant, Tariff tariff, LocalDate start, LocalDate end) {

    public Contract {
        Objects.requireNonNull(number, "number");
        Objects.requireNonNull(tenant, "tenant");
        Objects.requireNonNull(tariff, "tariff");
        Objects.requireNonNull(start, "start");
        if (end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("Ende vor Beginn bei " + number);
        }
    }

    public static Contract openEnded(String number, Tenant tenant, Tariff tariff, LocalDate start) {
        return new Contract(number, tenant, tariff, start, null);
    }

    public boolean isOpenEnded() {
        return end == null;
    }

    public boolean runsIn(YearMonth month) {
        boolean started = !YearMonth.from(start).isAfter(month);
        boolean notEnded = end == null || !YearMonth.from(end).isBefore(month);
        return started && notEnded;
    }

    public Contract withTariff(Tariff newTariff) {
        return new Contract(number, tenant, newTariff, start, end);
    }

    public Contract endedOn(LocalDate newEnd) {
        return new Contract(number, tenant, tariff, start, newEnd);
    }
}
