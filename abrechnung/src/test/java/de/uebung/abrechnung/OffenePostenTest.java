package de.uebung.abrechnung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uebung.hausfw.Ablage;
import de.uebung.hausfw.Betrag;
import de.uebung.hausfw.Konfiguration;
import de.uebung.hausfw.SpeicherAblage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OffenePostenTest {

    private final Ablage<Rechnung> rechnungen = new SpeicherAblage<>();
    private final OffenePosten posten = new OffenePosten(rechnungen);
    private Rechnung august;
    private Rechnung september;

    @BeforeEach
    void legeRechnungenAn() {
        RechnungsRechner rechner = new RechnungsRechner(Konfiguration.standard());
        august = rechner.berechne(Testdaten.basisvertrag(), YearMonth.of(2026, 8));
        september = rechner.berechne(Testdaten.basisvertrag(), YearMonth.of(2026, 9));
        rechnungen.speichere(august.id(), august);
        rechnungen.speichere(september.id(), september);
    }

    @Test
    void ohneZahlungIstDerVolleBetragOffen() {
        assertEquals(Betrag.euro("23,80"), posten.offen(september));
        assertFalse(posten.istBezahlt(september));
    }

    @Test
    void teilzahlungenWerdenAufsummiert() {
        posten.buche(september.id(), Betrag.euro("10,00"));
        posten.buche(september.id(), Betrag.euro("5,80"));

        assertEquals(Betrag.euro("15,80"), posten.gezahlt(september));
        assertEquals(Betrag.euro("8,00"), posten.offen(september));
    }

    @Test
    void ueberzahlungGiltAlsBezahlt() {
        posten.buche(september.id(), Betrag.euro("30,00"));

        assertTrue(posten.istBezahlt(september));
        assertEquals(Betrag.euro("-6,20"), posten.offen(september));
    }

    @Test
    void zahlungAufUnbekannteRechnungWirdAbgelehnt() {
        assertThrows(NoSuchElementException.class, () -> posten.buche("V-1001/2030-01", Betrag.euro("1")));
    }

    @Test
    void zahlungMussPositivSein() {
        assertThrows(IllegalArgumentException.class, () -> posten.buche(september.id(), Betrag.NULL));
        assertThrows(IllegalArgumentException.class, () -> posten.buche(september.id(), Betrag.euro("-1")));
    }

    @Test
    void ueberfaelligSindNurUnbezahlteRechnungenVorDemStichtag() {
        assertEquals(List.of(august), posten.ueberfaellig(LocalDate.of(2026, 9, 15)));
        assertEquals(List.of(august, september), posten.ueberfaellig(LocalDate.of(2026, 9, 16)));

        posten.buche(august.id(), Betrag.euro("23,80"));

        assertEquals(List.of(september), posten.ueberfaellig(LocalDate.of(2026, 9, 16)));
    }

    @Test
    void summeOffenUeberAlleRechnungen() {
        posten.buche(august.id(), Betrag.euro("20,00"));

        assertEquals(Betrag.euro("27,60"), posten.summeOffen());
    }
}
