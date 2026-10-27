package de.uebung.abrechnung;

import de.uebung.hausfw.Betrag;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

/**
 * Rechnungen als CSV für die Buchhaltung, Trennzeichen Semikolon, Beträge mit Dezimalkomma.
 */
public class Rechnungsexport {

    static final String KOPFZEILE = "rechnung;vertrag;mandant;monat;netto;steuer;rabatt;brutto;faellig";

    private static final DateTimeFormatter DATUM = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String alsCsv(List<Rechnung> rechnungen) {
        StringJoiner zeilen = new StringJoiner("\n", "", "\n");
        zeilen.add(KOPFZEILE);
        for (Rechnung rechnung : rechnungen) {
            zeilen.add(zeile(rechnung));
        }
        return zeilen.toString();
    }

    String zeile(Rechnung rechnung) {
        return String.join(";",
                rechnung.id(),
                rechnung.vertrag().nummer(),
                rechnung.vertrag().mandant().kennung(),
                rechnung.monat().toString(),
                dezimal(rechnung.netto()),
                dezimal(rechnung.steuer()),
                dezimal(rechnung.rabatt()),
                dezimal(rechnung.brutto()),
                rechnung.faellig().format(DATUM));
    }

    static String dezimal(Betrag betrag) {
        long cent = Math.abs(betrag.inCent());
        return "%s%d,%02d".formatted(betrag.istNegativ() ? "-" : "", cent / 100, cent % 100);
    }
}
