package de.exercise.billing;

import de.exercise.core.Money;
import java.math.BigDecimal;
import java.time.LocalDate;

final class TestData {

    static final Tenant NORDTAL = new Tenant("M-01", "Nordtal Versorgung eG");
    static final Tenant SUEDHANG = new Tenant("M-02", "Wohnbau Südhang GmbH");

    static final Tariff BASIC = Tariff.withoutDiscount("Basis", Money.euro("20,00"));
    static final Tariff COMFORT = new Tariff("Komfort", Money.euro("39,90"), new BigDecimal("15"));
    static final Tariff PREMIUM = new Tariff("Premium", Money.euro("59,90"), new BigDecimal("5"));

    private TestData() {
    }

    static Contract contract(String number, Tariff tariff, LocalDate start) {
        return Contract.openEnded(number, NORDTAL, tariff, start);
    }

    static Contract basicContract() {
        return contract("V-1001", BASIC, LocalDate.of(2026, 1, 1));
    }

    static Contract comfortContract() {
        return contract("V-1002", COMFORT, LocalDate.of(2025, 7, 1));
    }
}
