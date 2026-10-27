package de.uebung.abrechnung;

import de.uebung.hausfw.Betrag;
import java.math.BigDecimal;
import java.time.LocalDate;

final class Testdaten {

    static final Mandant NORDTAL = new Mandant("M-01", "Nordtal Versorgung eG");
    static final Mandant SUEDHANG = new Mandant("M-02", "Wohnbau Südhang GmbH");

    static final Tarif BASIS = Tarif.ohneRabatt("Basis", Betrag.euro("20,00"));
    static final Tarif KOMFORT = new Tarif("Komfort", Betrag.euro("39,90"), new BigDecimal("15"));
    static final Tarif PREMIUM = new Tarif("Premium", Betrag.euro("59,90"), new BigDecimal("5"));

    private Testdaten() {
    }

    static Vertrag vertrag(String nummer, Tarif tarif, LocalDate beginn) {
        return Vertrag.unbefristet(nummer, NORDTAL, tarif, beginn);
    }

    static Vertrag basisvertrag() {
        return vertrag("V-1001", BASIS, LocalDate.of(2026, 1, 1));
    }

    static Vertrag komfortvertrag() {
        return vertrag("V-1002", KOMFORT, LocalDate.of(2025, 7, 1));
    }
}
