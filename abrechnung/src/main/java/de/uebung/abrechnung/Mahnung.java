package de.uebung.abrechnung;

import de.uebung.hausfw.Betrag;
import java.time.LocalDate;

public record Mahnung(Rechnung rechnung, int stufe, Betrag gebuehr, LocalDate datum) {

    public Betrag forderung() {
        return rechnung.brutto().plus(gebuehr);
    }
}
