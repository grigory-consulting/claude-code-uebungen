package de.uebung.altbestand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Liest die Zahlungsdatei der Bank (feste Satzlaenge, ein Satz je Zeile).
 *
 * <pre>
 * Pos  0- 9  Vertragsnummer, rechts mit Leerzeichen aufgefuellt
 * Pos 10-17  Buchungsdatum TTMMJJJJ
 * Pos 18-29  Betrag in Cent, 12 Stellen mit fuehrenden Nullen
 * Pos 30     Kennzeichen S (Soll) oder H (Haben)
 * Pos 31-57  Verwendungszweck
 * </pre>
 *
 * @author abr
 */
public class ZahlungsdateiLeser {

    public static final int SATZLAENGE = 58;

    public static int fehlerhafteZeilen = 0;

    private String zeichensatz = "ISO-8859-1";

    public ZahlungsdateiLeser() {
    }

    public ZahlungsdateiLeser(String zeichensatz) {
        this.zeichensatz = zeichensatz;
    }

    /**
     * @param pfad Pfad zur Zahlungsdatei
     * @return Vector mit Zahlung-Objekten oder null, wenn die Datei nicht existiert
     */
    public Vector leseDatei(String pfad) {
        File datei = new File(pfad);
        if (!datei.exists()) {
            return null;
        }
        fehlerhafteZeilen = 0;
        Vector zahlungen = new Vector();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(datei), zeichensatz));
            String zeile = in.readLine();
            while (zeile != null) {
                if (zeile.length() != SATZLAENGE) {
                    fehlerhafteZeilen++;
                } else {
                    Zahlung z = leseSatz(zeile);
                    if (z != null) {
                        zahlungen.addElement(z);
                    } else {
                        fehlerhafteZeilen++;
                    }
                }
                zeile = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        korrigiereVorzeichen(zahlungen);
        return zahlungen;
    }

    protected Zahlung leseSatz(String zeile) {
        Zahlung z = new Zahlung();
        z.vertragsNr = zeile.substring(0, 10).trim();
        z.buchungsDatum = parseDatum(zeile.substring(10, 18));
        if (z.buchungsDatum == null) {
            return null;
        }
        try {
            z.betragCent = Long.parseLong(zeile.substring(18, 30));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        z.kennzeichen = zeile.substring(30, 31);
        z.verwendungszweck = zeile.substring(31, 58).trim();
        return z;
    }

    private Date parseDatum(String text) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        sdf.setLenient(false);
        try {
            return sdf.parse(text);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void korrigiereVorzeichen(Vector zahlungen) {
        for (int i = 0; i < zahlungen.size(); i++) {
            Zahlung z = (Zahlung) zahlungen.elementAt(i);
            if ("H".equals(z.kennzeichen)) {
                z.betragCent = -z.betragCent;
            }
        }
    }
}
