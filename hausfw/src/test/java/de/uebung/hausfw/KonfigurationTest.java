package de.uebung.hausfw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class KonfigurationTest {

    @Test
    void standardLiestDateiAusDemKlassenpfad() {
        Konfiguration konfiguration = Konfiguration.standard();
        assertEquals(new BigDecimal("7"), konfiguration.steuersatzProzent());
        assertEquals(Betrag.euro("2,50"), konfiguration.mahngebuehr());
        assertEquals(10, konfiguration.mahnfristTage());
    }

    @Test
    void fehlenderSchluesselFuehrtZuKlarerMeldung() {
        Properties werte = new Properties();
        werte.setProperty("steuersatz.prozent", "19");
        Konfiguration konfiguration = Konfiguration.aus(werte);

        IllegalStateException fehler = assertThrows(IllegalStateException.class, konfiguration::mahnfristTage);
        assertEquals("Einstellung fehlt: mahnfrist.tage", fehler.getMessage());
    }

    @Test
    void aenderungenAnDenQuellwertenWirkenNichtNach() {
        Properties werte = new Properties();
        werte.setProperty("mahnfrist.tage", "14");
        Konfiguration konfiguration = Konfiguration.aus(werte);
        werte.setProperty("mahnfrist.tage", "1");

        assertEquals(14, konfiguration.mahnfristTage());
    }
}
