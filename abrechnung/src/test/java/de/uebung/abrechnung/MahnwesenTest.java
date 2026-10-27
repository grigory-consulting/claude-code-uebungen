package de.uebung.abrechnung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uebung.hausfw.Betrag;
import de.uebung.hausfw.Konfiguration;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class MahnwesenTest {

    private final Konfiguration konfiguration = Konfiguration.standard();
    private final Mahnwesen mahnwesen = new Mahnwesen(konfiguration);
    private final Rechnung rechnung = new RechnungsRechner(konfiguration)
            .berechne(Testdaten.basisvertrag(), YearMonth.of(2026, 9));

    @Test
    void keineMahnungInnerhalbDerFrist() {
        assertTrue(mahnwesen.pruefe(rechnung, LocalDate.of(2026, 9, 29), false).isEmpty());
    }

    @Test
    void ersteMahnungAmTagNachFristende() {
        Mahnung mahnung = mahnwesen.pruefe(rechnung, LocalDate.of(2026, 9, 30), false).orElseThrow();

        assertEquals(1, mahnung.stufe());
        assertEquals(Betrag.euro("5,00"), mahnung.gebuehr());
        assertEquals(LocalDate.of(2026, 9, 30), mahnung.datum());
        assertEquals(Betrag.euro("28,80"), mahnung.forderung());
    }

    @Test
    void bezahlteRechnungWirdNichtGemahnt() {
        assertTrue(mahnwesen.pruefe(rechnung, LocalDate.of(2026, 12, 1), true).isEmpty());
    }

    @Test
    void zweiteStufeNachEinerWeiterenFrist() {
        Mahnung mahnung = mahnwesen.pruefe(rechnung, LocalDate.of(2026, 10, 14), false).orElseThrow();

        assertEquals(2, mahnung.stufe());
        assertEquals(Betrag.euro("10,00"), mahnung.gebuehr());
    }

    @Test
    void hoechstensDritteStufe() {
        Mahnung mahnung = mahnwesen.pruefe(rechnung, LocalDate.of(2027, 3, 1), false).orElseThrow();

        assertEquals(3, mahnung.stufe());
        assertEquals(Betrag.euro("15,00"), mahnung.gebuehr());
    }
}
