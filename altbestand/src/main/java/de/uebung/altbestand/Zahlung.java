package de.uebung.altbestand;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Eine Zahlung aus der Zahlungsdatei.
 *
 * @author abr
 */
public class Zahlung implements Serializable {

    private static final long serialVersionUID = 1L;

    public String vertragsNr;
    public Date buchungsDatum;
    public long betragCent;
    public String kennzeichen;
    public String verwendungszweck;

    public Zahlung() {
    }

    public String getVertragsNr() {
        return vertragsNr;
    }

    public void setVertragsNr(String vertragsNr) {
        this.vertragsNr = vertragsNr;
    }

    public Date getBuchungsDatum() {
        return buchungsDatum;
    }

    public void setBuchungsDatum(Date buchungsDatum) {
        this.buchungsDatum = buchungsDatum;
    }

    public long getBetragCent() {
        return betragCent;
    }

    public void setBetragCent(long betragCent) {
        this.betragCent = betragCent;
    }

    public String getKennzeichen() {
        return kennzeichen;
    }

    public void setKennzeichen(String kennzeichen) {
        this.kennzeichen = kennzeichen;
    }

    public String getVerwendungszweck() {
        return verwendungszweck;
    }

    public void setVerwendungszweck(String verwendungszweck) {
        this.verwendungszweck = verwendungszweck;
    }

    public boolean isGutschrift() {
        return "H".equals(kennzeichen);
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String datum = buchungsDatum != null ? sdf.format(buchungsDatum) : "?";
        return "Zahlung[" + vertragsNr + ", " + datum + ", " + betragCent + ", " + kennzeichen + ", "
                + verwendungszweck + "]";
    }
}
