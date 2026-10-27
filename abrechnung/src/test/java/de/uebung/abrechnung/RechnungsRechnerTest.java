package de.uebung.abrechnung;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.uebung.hausfw.Betrag;
import de.uebung.hausfw.Konfiguration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class RechnungsRechnerTest {

    private static final YearMonth SEPTEMBER = YearMonth.of(2026, 9);

    private final RechnungsRechner rechner = new RechnungsRechner(Konfiguration.standard());

    @Test
    void basistarifOhneRabatt() {
        Rechnung rechnung = rechner.berechne(Testdaten.basisvertrag(), SEPTEMBER);

        assertAll(
                () -> assertEquals(Betrag.euro("20,00"), rechnung.netto()),
                () -> assertEquals(Betrag.euro("3,80"), rechnung.steuer()),
                () -> assertEquals(Betrag.NULL, rechnung.rabatt()),
                () -> assertEquals(Betrag.euro("23,80"), rechnung.brutto()));
    }

    @Test
    void komforttarifMitFuenfzehnProzentRabatt() {
        Rechnung rechnung = rechner.berechne(Testdaten.komfortvertrag(), SEPTEMBER);

        assertAll(
                () -> assertEquals(Betrag.euro("39,90"), rechnung.netto()),
                () -> assertEquals(Betrag.euro("7,58"), rechnung.steuer()),
                () -> assertEquals(Betrag.euro("7,12"), rechnung.rabatt()),
                () -> assertEquals(Betrag.euro("40,36"), rechnung.brutto()));
    }

    @Test
    void premiumtarifMitFuenfProzentRabatt() {
        Vertrag vertrag = Testdaten.vertrag("V-1003", Testdaten.PREMIUM, LocalDate.of(2024, 3, 1));

        Rechnung rechnung = rechner.berechne(vertrag, SEPTEMBER);

        assertAll(
                () -> assertEquals(Betrag.euro("11,38"), rechnung.steuer()),
                () -> assertEquals(Betrag.euro("3,56"), rechnung.rabatt()),
                () -> assertEquals(Betrag.euro("67,72"), rechnung.brutto()));
    }

    @Test
    void faelligAmFuenfzehntenDesAbrechnungsmonats() {
        Rechnung rechnung = rechner.berechne(Testdaten.basisvertrag(), YearMonth.of(2027, 2));

        assertEquals(LocalDate.of(2027, 2, 15), rechnung.faellig());
    }

    @Test
    void steuersatzKommtAusDerKonfiguration() {
        Properties werte = new Properties();
        werte.setProperty("steuersatz.prozent", "7");
        RechnungsRechner ermaessigt = new RechnungsRechner(Konfiguration.aus(werte));

        Rechnung rechnung = ermaessigt.berechne(Testdaten.basisvertrag(), SEPTEMBER);

        assertEquals(Betrag.euro("1,40"), rechnung.steuer());
        assertEquals(Betrag.euro("21,40"), rechnung.brutto());
    }

    @Test
    void monatVorVertragsbeginnIstUnzulaessig() {
        Vertrag vertrag = Testdaten.vertrag("V-1004", Testdaten.BASIS, LocalDate.of(2026, 10, 1));

        assertThrows(IllegalArgumentException.class, () -> rechner.berechne(vertrag, SEPTEMBER));
    }

    @Test
    void monatNachVertragsendeIstUnzulaessig() {
        Vertrag vertrag = Testdaten.basisvertrag().beendetZum(LocalDate.of(2026, 8, 31));

        assertThrows(IllegalArgumentException.class, () -> rechner.berechne(vertrag, SEPTEMBER));
    }

    @Test
    void letzterVertragsmonatWirdAbgerechnet() {
        Vertrag vertrag = Testdaten.basisvertrag().beendetZum(LocalDate.of(2026, 9, 30));

        assertEquals(Betrag.euro("23,80"), rechner.berechne(vertrag, SEPTEMBER).brutto());
    }
}
