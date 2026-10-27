package de.uebung.abrechnung;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uebung.hausfw.Betrag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class VertragTest {

    @Test
    void laeuftAbBeginnmonatBisEndmonat() {
        Vertrag vertrag = Testdaten.vertrag("V-2001", Testdaten.BASIS, LocalDate.of(2026, 3, 20))
                .beendetZum(LocalDate.of(2026, 6, 10));

        assertFalse(vertrag.laeuftIm(YearMonth.of(2026, 2)));
        assertTrue(vertrag.laeuftIm(YearMonth.of(2026, 3)));
        assertTrue(vertrag.laeuftIm(YearMonth.of(2026, 6)));
        assertFalse(vertrag.laeuftIm(YearMonth.of(2026, 7)));
    }

    @Test
    void unbefristeterVertragLaeuftWeiter() {
        Vertrag vertrag = Testdaten.basisvertrag();

        assertTrue(vertrag.istUnbefristet());
        assertTrue(vertrag.laeuftIm(YearMonth.of(2040, 1)));
    }

    @Test
    void endeVorBeginnIstUnzulaessig() {
        assertThrows(IllegalArgumentException.class, () -> new Vertrag("V-2002", Testdaten.NORDTAL,
                Testdaten.BASIS, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 30)));
    }

    @Test
    void rabattUeberHundertProzentIstUnzulaessig() {
        assertThrows(IllegalArgumentException.class,
                () -> new Tarif("Falsch", Betrag.euro("10"), new BigDecimal("100.01")));
    }
}
