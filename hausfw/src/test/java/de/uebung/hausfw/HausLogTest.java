package de.uebung.hausfw;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HausLogTest {

    @Test
    void platzhalterWerdenDerReiheNachGefuellt() {
        assertEquals("Vertrag V-1 über 5,00 EUR",
                HausLog.fuelle("Vertrag {} über {}", "V-1", Betrag.euro("5")));
    }

    @Test
    void ueberzaehligePlatzhalterBleibenStehen() {
        assertEquals("a=1 b={}", HausLog.fuelle("a={} b={}", 1));
    }

    @Test
    void ueberzaehligeWerteWerdenIgnoriert() {
        assertEquals("nur 1", HausLog.fuelle("nur {}", 1, 2, 3));
    }
}
