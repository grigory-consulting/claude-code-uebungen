package de.uebung.abrechnung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.uebung.hausfw.Betrag;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TarifkatalogTest {

    private final Tarifkatalog katalog = new Tarifkatalog();

    @BeforeEach
    void befuelle() {
        katalog.hinterlege(Testdaten.NORDTAL, Testdaten.PREMIUM);
        katalog.hinterlege(Testdaten.NORDTAL, Testdaten.BASIS);
        katalog.hinterlege(Testdaten.NORDTAL, Testdaten.KOMFORT);
        katalog.hinterlege(Testdaten.SUEDHANG, Tarif.ohneRabatt("Basis", Betrag.euro("18,50")));
    }

    @Test
    void tarifeSindNachMonatsbeitragSortiert() {
        assertEquals(List.of(Testdaten.BASIS, Testdaten.KOMFORT, Testdaten.PREMIUM), katalog.tarife(Testdaten.NORDTAL));
    }

    @Test
    void gleicherTarifnameJeMandantVerschieden() {
        assertEquals(Betrag.euro("20,00"), katalog.tarif(Testdaten.NORDTAL, "Basis").orElseThrow().monatsbeitrag());
        assertEquals(Betrag.euro("18,50"), katalog.tarif(Testdaten.SUEDHANG, "Basis").orElseThrow().monatsbeitrag());
    }

    @Test
    void guenstigsterTarifDesMandanten() {
        assertEquals("Basis", katalog.guenstigster(Testdaten.SUEDHANG).name());
    }

    @Test
    void mandantOhneTarifeHatKeinenGuenstigsten() {
        Mandant neu = new Mandant("M-99", "Neukunde Ost");

        assertTrue(katalog.tarife(neu).isEmpty());
        assertThrows(NoSuchElementException.class, () -> katalog.guenstigster(neu));
    }

    @Test
    void neuerVertragNurMitBekanntemTarif() {
        Vertrag vertrag = katalog.neuerVertrag("V-4711", Testdaten.NORDTAL, "Komfort", LocalDate.of(2025, 4, 1));

        assertEquals(Testdaten.KOMFORT, vertrag.tarif());
        assertTrue(vertrag.istUnbefristet());
        assertThrows(NoSuchElementException.class,
                () -> katalog.neuerVertrag("V-3002", Testdaten.SUEDHANG, "Premium", LocalDate.of(2026, 10, 1)));
    }
}
