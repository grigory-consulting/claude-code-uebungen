package de.uebung.abrechnung;

import de.uebung.hausfw.Betrag;
import java.math.BigDecimal;
import java.util.Objects;

public record Tarif(String name, Betrag monatsbeitrag, BigDecimal rabattProzent) {

    private static final BigDecimal HUNDERT = BigDecimal.valueOf(100);

    public Tarif {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(monatsbeitrag, "monatsbeitrag");
        Objects.requireNonNull(rabattProzent, "rabattProzent");
        if (monatsbeitrag.istNegativ()) {
            throw new IllegalArgumentException("Monatsbeitrag darf nicht negativ sein: " + monatsbeitrag);
        }
        if (rabattProzent.signum() < 0 || rabattProzent.compareTo(HUNDERT) > 0) {
            throw new IllegalArgumentException("Rabatt außerhalb 0 bis 100 Prozent: " + rabattProzent);
        }
    }

    public static Tarif ohneRabatt(String name, Betrag monatsbeitrag) {
        return new Tarif(name, monatsbeitrag, BigDecimal.ZERO);
    }

    public boolean hatRabatt() {
        return rabattProzent.signum() > 0;
    }
}
