package de.uebung.hausfw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SpeicherAblageTest {

    private final Ablage<String> ablage = new SpeicherAblage<>();

    @Test
    void gespeichertesObjektLaesstSichLaden() {
        ablage.speichere("a", "Anton");
        assertEquals(Optional.of("Anton"), ablage.lade("a"));
    }

    @Test
    void unbekannteIdLiefertLeeresOptional() {
        assertTrue(ablage.lade("fehlt").isEmpty());
    }

    @Test
    void erneutesSpeichernErsetztUndBehaeltReihenfolge() {
        ablage.speichere("a", "eins");
        ablage.speichere("b", "zwei");
        ablage.speichere("a", "drei");
        assertEquals(List.of("drei", "zwei"), ablage.alle());
    }

    @Test
    void nullWirdAbgelehnt() {
        assertThrows(NullPointerException.class, () -> ablage.speichere("x", null));
    }
}
