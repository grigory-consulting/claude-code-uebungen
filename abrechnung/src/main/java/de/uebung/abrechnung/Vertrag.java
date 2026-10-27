package de.uebung.abrechnung;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Vertrag eines Mandanten. {@code ende == null} bedeutet unbefristet.
 */
public record Vertrag(String nummer, Mandant mandant, Tarif tarif, LocalDate beginn, LocalDate ende) {

    public Vertrag {
        Objects.requireNonNull(nummer, "nummer");
        Objects.requireNonNull(mandant, "mandant");
        Objects.requireNonNull(tarif, "tarif");
        Objects.requireNonNull(beginn, "beginn");
        if (ende != null && ende.isBefore(beginn)) {
            throw new IllegalArgumentException("Ende vor Beginn bei " + nummer);
        }
    }

    public static Vertrag unbefristet(String nummer, Mandant mandant, Tarif tarif, LocalDate beginn) {
        return new Vertrag(nummer, mandant, tarif, beginn, null);
    }

    public boolean istUnbefristet() {
        return ende == null;
    }

    public boolean laeuftIm(YearMonth monat) {
        boolean begonnen = !YearMonth.from(beginn).isAfter(monat);
        boolean nichtBeendet = ende == null || !YearMonth.from(ende).isBefore(monat);
        return begonnen && nichtBeendet;
    }

    public Vertrag mitTarif(Tarif neuerTarif) {
        return new Vertrag(nummer, mandant, neuerTarif, beginn, ende);
    }

    public Vertrag beendetZum(LocalDate neuesEnde) {
        return new Vertrag(nummer, mandant, tarif, beginn, neuesEnde);
    }
}
