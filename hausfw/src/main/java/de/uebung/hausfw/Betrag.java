package de.uebung.hausfw;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Geldbetrag in Euro, intern in Cent gespeichert. Unveränderlich.
 */
public final class Betrag implements Comparable<Betrag> {

    public static final Betrag NULL = new Betrag(0);

    private static final BigDecimal HUNDERT = BigDecimal.valueOf(100);

    private final long cent;

    private Betrag(long cent) {
        this.cent = cent;
    }

    public static Betrag cent(long cent) {
        return cent == 0 ? NULL : new Betrag(cent);
    }

    /**
     * Liest einen Eurobetrag wie {@code "12,34"}, {@code "12.34"} oder {@code "-5"}.
     */
    public static Betrag euro(String text) {
        Objects.requireNonNull(text, "text");
        String bereinigt = text.trim().replace(',', '.');
        if (bereinigt.isEmpty()) {
            throw new IllegalArgumentException("Leerer Betrag");
        }
        BigDecimal wert;
        try {
            wert = new BigDecimal(bereinigt);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Kein gültiger Betrag: " + text, e);
        }
        if (wert.scale() > 2) {
            throw new IllegalArgumentException("Mehr als zwei Nachkommastellen: " + text);
        }
        return cent(wert.movePointRight(2).longValueExact());
    }

    public Betrag plus(Betrag anderer) {
        return cent(Math.addExact(cent, anderer.cent));
    }

    public Betrag minus(Betrag anderer) {
        return cent(Math.subtractExact(cent, anderer.cent));
    }

    public Betrag mal(BigDecimal faktor, Rundung rundung) {
        BigDecimal ergebnis = BigDecimal.valueOf(cent).multiply(faktor);
        return cent(ergebnis.setScale(0, rundung.modus()).longValueExact());
    }

    public Betrag prozent(BigDecimal prozent, Rundung rundung) {
        BigDecimal ergebnis = BigDecimal.valueOf(cent).multiply(prozent).divide(HUNDERT);
        return cent(ergebnis.setScale(0, rundung.modus()).longValueExact());
    }

    public Betrag geteilt(int teiler, Rundung rundung) {
        if (teiler <= 0) {
            throw new IllegalArgumentException("Teiler muss positiv sein: " + teiler);
        }
        BigDecimal ergebnis = BigDecimal.valueOf(cent).divide(BigDecimal.valueOf(teiler), 0, rundung.modus());
        return cent(ergebnis.longValueExact());
    }

    public boolean istNegativ() {
        return cent < 0;
    }

    public long inCent() {
        return cent;
    }

    @Override
    public int compareTo(Betrag anderer) {
        return Long.compare(cent, anderer.cent);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Betrag anderer && cent == anderer.cent;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(cent);
    }

    @Override
    public String toString() {
        long betrag = Math.abs(cent);
        String vorzeichen = cent < 0 ? "-" : "";
        return "%s%d,%02d EUR".formatted(vorzeichen, betrag / 100, betrag % 100);
    }
}
