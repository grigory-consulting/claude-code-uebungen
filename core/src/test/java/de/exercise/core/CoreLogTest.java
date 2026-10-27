package de.exercise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CoreLogTest {

    @Test
    void placeholdersAreFilledInOrder() {
        assertEquals("Vertrag V-1 über 5,00 EUR",
                CoreLog.fill("Vertrag {} über {}", "V-1", Money.euro("5")));
    }

    @Test
    void extraPlaceholdersRemain() {
        assertEquals("a=1 b={}", CoreLog.fill("a={} b={}", 1));
    }

    @Test
    void extraValuesAreIgnored() {
        assertEquals("nur 1", CoreLog.fill("nur {}", 1, 2, 3));
    }
}
