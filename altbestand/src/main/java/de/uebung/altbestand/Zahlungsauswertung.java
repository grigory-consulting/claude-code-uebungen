package de.uebung.altbestand;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

/**
 * Summiert die Zahlungen einer Datei je Vertrag und gibt eine Liste aus.
 *
 * @author abr
 */
public class Zahlungsauswertung {

    public Hashtable summeJeVertrag(Vector zahlungen) {
        Hashtable summen = new Hashtable();
        if (zahlungen == null) {
            return summen;
        }
        for (int i = 0; i < zahlungen.size(); i++) {
            Zahlung z = (Zahlung) zahlungen.elementAt(i);
            Long bisher = (Long) summen.get(z.vertragsNr);
            if (bisher == null) {
                bisher = new Long(0);
            }
            summen.put(z.vertragsNr, new Long(bisher.longValue() + z.betragCent));
        }
        return summen;
    }

    public long gesamtsumme(Vector zahlungen) {
        long summe = 0;
        if (zahlungen != null) {
            for (int i = 0; i < zahlungen.size(); i++) {
                summe += ((Zahlung) zahlungen.elementAt(i)).betragCent;
            }
        }
        return summe;
    }

    public Vector gutschriften(Vector zahlungen) {
        Vector ergebnis = new Vector();
        for (int i = 0; i < zahlungen.size(); i++) {
            Zahlung z = (Zahlung) zahlungen.elementAt(i);
            if (z.isGutschrift()) {
                ergebnis.addElement(z);
            }
        }
        return ergebnis;
    }

    public static String formatiereCent(long cent) {
        DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.GERMANY));
        return df.format(cent / 100.0);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Aufruf: Zahlungsauswertung <datei>");
            return;
        }
        ZahlungsdateiLeser leser = new ZahlungsdateiLeser();
        Vector zahlungen = leser.leseDatei(args[0]);
        if (zahlungen == null) {
            System.out.println("Datei nicht gefunden: " + args[0]);
            return;
        }
        Zahlungsauswertung auswertung = new Zahlungsauswertung();
        Hashtable summen = auswertung.summeJeVertrag(zahlungen);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        for (int i = 0; i < zahlungen.size(); i++) {
            Zahlung z = (Zahlung) zahlungen.elementAt(i);
            System.out.println(z.vertragsNr + "  " + sdf.format(z.buchungsDatum) + "  "
                    + formatiereCent(z.betragCent) + "  " + z.verwendungszweck);
        }
        System.out.println("--");
        Enumeration e = summen.keys();
        while (e.hasMoreElements()) {
            String nr = (String) e.nextElement();
            System.out.println(nr + "  " + formatiereCent(((Long) summen.get(nr)).longValue()));
        }
        System.out.println("Gesamt: " + formatiereCent(auswertung.gesamtsumme(zahlungen)));
        System.out.println("Fehlerhafte Zeilen: " + ZahlungsdateiLeser.fehlerhafteZeilen);
    }
}
