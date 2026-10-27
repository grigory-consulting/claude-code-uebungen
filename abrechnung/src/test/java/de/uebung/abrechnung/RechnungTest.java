package de.uebung.abrechnung;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.uebung.hausfw.Betrag;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class RechnungTest {

    private final Rechnung rechnung = new Rechnung(
            Testdaten.basisvertrag(),
            YearMonth.of(2026, 9),
            Betrag.euro("20,00"),
            Betrag.euro("3,80"),
            Betrag.NULL,
            Betrag.euro("23,80"),
            LocalDate.of(2026, 9, 15));

    @Test
    void alsTextNenntMonatVertragUndBetrag() {
        assertEquals("Rechnung 2026-09 · V-1001 · 23,80 EUR", rechnung.alsText());
    }

    @Test
    void idBestehtAusVertragUndMonat() {
        assertEquals("V-1001/2026-09", rechnung.id());
    }
}
