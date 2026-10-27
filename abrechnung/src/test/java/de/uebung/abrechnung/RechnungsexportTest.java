package de.uebung.abrechnung;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.uebung.hausfw.Betrag;
import de.uebung.hausfw.Konfiguration;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;

class RechnungsexportTest {

    private final RechnungsRechner rechner = new RechnungsRechner(Konfiguration.standard());
    private final Rechnungsexport export = new Rechnungsexport();

    @Test
    void eineZeileJeRechnungMitKopfzeile() {
        Rechnung rechnung = rechner.berechne(Testdaten.komfortvertrag(), YearMonth.of(2026, 9));

        String csv = export.alsCsv(List.of(rechnung));

        assertEquals(Rechnungsexport.KOPFZEILE + "\n"
                + "V-1002/2026-09;V-1002;M-01;2026-09;39,90;7,58;7,12;40,36;15.09.2026\n", csv);
    }

    @Test
    void leereListeErgibtNurKopfzeile() {
        assertEquals(Rechnungsexport.KOPFZEILE + "\n", export.alsCsv(List.of()));
    }

    @Test
    void exportEnthaeltKeineMandantennamen() {
        Rechnung rechnung = rechner.berechne(Testdaten.basisvertrag(), YearMonth.of(2026, 9));

        String zeile = export.zeile(rechnung);

        assertEquals(-1, zeile.indexOf(Testdaten.NORDTAL.name()));
    }

    @Test
    void dezimalMitKommaUndVorzeichen() {
        assertEquals("0,07", Rechnungsexport.dezimal(Betrag.cent(7)));
        assertEquals("-12,50", Rechnungsexport.dezimal(Betrag.euro("-12,5")));
        assertEquals("1500,00", Rechnungsexport.dezimal(Betrag.euro("1500")));
    }
}
