package de.uebung.abrechnung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uebung.hausfw.Betrag;
import de.uebung.hausfw.Konfiguration;
import de.uebung.hausfw.SpeicherAblage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VertragsServiceTest {

    private static final YearMonth SEPTEMBER = YearMonth.of(2026, 9);

    private final ByteArrayOutputStream protokoll = new ByteArrayOutputStream();
    private PrintStream stderrVorher;
    private VertragsService service;

    @BeforeEach
    void richteEin() {
        stderrVorher = System.err;
        System.setErr(new PrintStream(protokoll, true, StandardCharsets.UTF_8));
        Konfiguration konfiguration = Konfiguration.standard();
        service = new VertragsService(new SpeicherAblage<>(), new SpeicherAblage<>(),
                new RechnungsRechner(konfiguration), new Mahnwesen(konfiguration));
        service.anlegen(Testdaten.basisvertrag());
        service.anlegen(Testdaten.komfortvertrag());
    }

    @AfterEach
    void raeumeAuf() {
        System.setErr(stderrVorher);
    }

    @Test
    void vertragDarfNurEinmalAngelegtWerden() {
        assertThrows(IllegalStateException.class, () -> service.anlegen(Testdaten.basisvertrag()));
    }

    @Test
    void unbekannterVertragWirdGemeldet() {
        assertThrows(NoSuchElementException.class, () -> service.rechneAb("V-9999", SEPTEMBER));
    }

    @Test
    void zweiteAbrechnungLiefertDieGespeicherteRechnung() {
        Rechnung erste = service.rechneAb("V-1001", SEPTEMBER);
        Rechnung zweite = service.rechneAb("V-1001", SEPTEMBER);

        assertSame(erste, zweite);
        assertEquals(1, service.rechnungenZu("V-1001").size());
    }

    @Test
    void monatslaufRechnetNurLaufendeVertraegeAb() {
        service.anlegen(Testdaten.vertrag("V-1005", Testdaten.PREMIUM, LocalDate.of(2026, 11, 1)));

        List<Rechnung> rechnungen = service.monatslauf(SEPTEMBER);

        assertEquals(2, rechnungen.size());
        assertEquals(Betrag.euro("64,16"), VertragsService.summe(rechnungen));
    }

    @Test
    void tarifwechselWirktAufDieNaechsteRechnung() {
        service.rechneAb("V-1001", SEPTEMBER);
        service.wechsleTarif("V-1001", Testdaten.KOMFORT);

        Rechnung oktober = service.rechneAb("V-1001", SEPTEMBER.plusMonths(1));

        assertEquals(Betrag.euro("40,36"), oktober.brutto());
        assertEquals(Betrag.euro("23,80"), service.rechnungenZu("V-1001").get(0).brutto());
    }

    @Test
    void mahnungFuerUnbezahlteRechnung() {
        service.rechneAb("V-1002", SEPTEMBER);

        assertTrue(service.pruefeMahnung("V-1002", SEPTEMBER, LocalDate.of(2026, 10, 1), false).isPresent());
        assertFalse(service.pruefeMahnung("V-1002", SEPTEMBER, LocalDate.of(2026, 10, 1), true).isPresent());
    }

    @Test
    void mahnlaufMahntNurUnbezahlteUndUeberfaelligeRechnungen() {
        SpeicherAblage<Rechnung> ablage = new SpeicherAblage<>();
        Konfiguration konfiguration = Konfiguration.standard();
        VertragsService mitAblage = new VertragsService(new SpeicherAblage<>(), ablage,
                new RechnungsRechner(konfiguration), new Mahnwesen(konfiguration));
        mitAblage.anlegen(Testdaten.basisvertrag());
        mitAblage.anlegen(Testdaten.komfortvertrag());
        mitAblage.monatslauf(SEPTEMBER);
        OffenePosten posten = new OffenePosten(ablage);
        posten.buche("V-1001/2026-09", Betrag.euro("23,80"));

        List<Mahnung> mahnungen = mitAblage.mahnlauf(posten, LocalDate.of(2026, 10, 2));

        assertEquals(1, mahnungen.size());
        assertEquals("V-1002", mahnungen.get(0).rechnung().vertrag().nummer());
    }

    @Test
    void ratenplanUeberDenRechnungsbetrag() {
        List<Betrag> raten = service.ratenplan("V-1001", SEPTEMBER, 2);

        assertEquals(List.of(Betrag.euro("11,90"), Betrag.euro("11,90")), raten);
    }

    @Test
    void protokollEnthaeltKeineMandantennamen() {
        service.monatslauf(SEPTEMBER);
        service.pruefeMahnung("V-1001", SEPTEMBER, LocalDate.of(2026, 11, 1), false);

        String text = protokoll.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("V-1001"));
        assertFalse(text.contains(Testdaten.NORDTAL.name()));
    }
}
