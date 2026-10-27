package de.uebung.abrechnung;

import de.uebung.hausfw.Betrag;
import de.uebung.hausfw.Rundung;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Ratenplan {

    static final int HOECHSTZAHL_RATEN = 12;

    public List<Betrag> raten(Betrag gesamt, int anzahl) {
        Objects.requireNonNull(gesamt, "gesamt");
        if (anzahl < 1 || anzahl > HOECHSTZAHL_RATEN) {
            throw new IllegalArgumentException(
                    "Anzahl der Raten muss zwischen 1 und %d liegen: %d".formatted(HOECHSTZAHL_RATEN, anzahl));
        }
        if (gesamt.istNegativ()) {
            throw new IllegalArgumentException("Ratenzahlung nur für positive Beträge: " + gesamt);
        }
        Betrag rate = gesamt.geteilt(anzahl, Rundung.KAUFMAENNISCH);
        List<Betrag> raten = new ArrayList<>(anzahl);
        for (int i = 0; i < anzahl; i++) {
            raten.add(rate);
        }
        return Collections.unmodifiableList(raten);
    }
}
