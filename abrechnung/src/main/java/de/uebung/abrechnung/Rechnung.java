package de.uebung.abrechnung;

import de.uebung.hausfw.Betrag;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public record Rechnung(
        Vertrag vertrag,
        YearMonth monat,
        Betrag netto,
        Betrag steuer,
        Betrag rabatt,
        Betrag brutto,
        LocalDate faellig) {

    public Rechnung {
        Objects.requireNonNull(vertrag, "vertrag");
        Objects.requireNonNull(monat, "monat");
        Objects.requireNonNull(netto, "netto");
        Objects.requireNonNull(steuer, "steuer");
        Objects.requireNonNull(rabatt, "rabatt");
        Objects.requireNonNull(brutto, "brutto");
        Objects.requireNonNull(faellig, "faellig");
    }

    public String id() {
        return vertrag.nummer() + "/" + monat;
    }

    public String alsText() {
        return "Rechnung %s · %s · %s".formatted(monat, vertrag.nummer(), brutto);
    }
}
