package de.uebung.hausfw;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Zentrale Einstellungen aus {@code abrechnung.properties} im Klassenpfad.
 */
public final class Konfiguration {

    static final String DATEI = "abrechnung.properties";

    private final Properties werte;

    private Konfiguration(Properties werte) {
        this.werte = werte;
    }

    public static Konfiguration standard() {
        try (InputStream ein = Konfiguration.class.getClassLoader().getResourceAsStream(DATEI)) {
            if (ein == null) {
                throw new IllegalStateException(DATEI + " nicht im Klassenpfad gefunden");
            }
            Properties werte = new Properties();
            werte.load(ein);
            return new Konfiguration(werte);
        } catch (IOException e) {
            throw new UncheckedIOException("Konfiguration nicht lesbar", e);
        }
    }

    public static Konfiguration aus(Properties werte) {
        Properties kopie = new Properties();
        kopie.putAll(werte);
        return new Konfiguration(kopie);
    }

    public BigDecimal steuersatzProzent() {
        return new BigDecimal(pflichtwert("steuersatz.prozent"));
    }

    public Betrag mahngebuehr() {
        return Betrag.euro(pflichtwert("mahngebuehr.euro"));
    }

    public int mahnfristTage() {
        return Integer.parseInt(pflichtwert("mahnfrist.tage"));
    }

    private String pflichtwert(String schluessel) {
        String wert = werte.getProperty(schluessel);
        if (wert == null || wert.isBlank()) {
            throw new IllegalStateException("Einstellung fehlt: " + schluessel);
        }
        return wert.trim();
    }
}
