package de.uebung.abrechnung;

import de.uebung.hausfw.Betrag;
import de.uebung.hausfw.Konfiguration;
import de.uebung.hausfw.Rundung;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class RechnungsRechner {

    static final int FAELLIGKEITSTAG = 15;

    private final BigDecimal steuersatzProzent;

    public RechnungsRechner(Konfiguration konfiguration) {
        this.steuersatzProzent = konfiguration.steuersatzProzent();
    }

    public Rechnung berechne(Vertrag vertrag, YearMonth monat) {
        Objects.requireNonNull(vertrag, "vertrag");
        Objects.requireNonNull(monat, "monat");
        pruefeLaufzeit(vertrag, monat);

        Tarif tarif = vertrag.tarif();
        Betrag netto = tarif.monatsbeitrag();
        Betrag steuer = netto.prozent(steuersatzProzent, Rundung.KAUFMAENNISCH);
        Betrag vorlaeufig = netto.plus(steuer);
        Betrag rabatt = vorlaeufig.prozent(tarif.rabattProzent(), Rundung.KAUFMAENNISCH);
        Betrag brutto = vorlaeufig.minus(rabatt);

        return new Rechnung(vertrag, monat, netto, steuer, rabatt, brutto, faelligkeit(monat));
    }

    private static void pruefeLaufzeit(Vertrag vertrag, YearMonth monat) {
        if (monat.isBefore(YearMonth.from(vertrag.beginn()))) {
            throw new IllegalArgumentException(
                    "Vertrag %s beginnt erst am %s".formatted(vertrag.nummer(), vertrag.beginn()));
        }
        if (vertrag.ende() != null && monat.isAfter(YearMonth.from(vertrag.ende()))) {
            throw new IllegalArgumentException(
                    "Vertrag %s endete am %s".formatted(vertrag.nummer(), vertrag.ende()));
        }
    }

    private static LocalDate faelligkeit(YearMonth monat) {
        return monat.atDay(FAELLIGKEITSTAG);
    }
}
