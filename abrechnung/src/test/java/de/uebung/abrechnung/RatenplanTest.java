package de.uebung.abrechnung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.uebung.hausfw.Betrag;
import java.util.List;
import org.junit.jupiter.api.Test;

class RatenplanTest {

    private final Ratenplan ratenplan = new Ratenplan();

    @Test
    void hundertEuroInVierRaten() {
        List<Betrag> raten = ratenplan.raten(Betrag.euro("100,00"), 4);

        assertEquals(List.of(
                Betrag.euro("25,00"), Betrag.euro("25,00"), Betrag.euro("25,00"), Betrag.euro("25,00")), raten);
    }

    @Test
    void alleRatenSindGleichHoch() {
        assertEquals(List.of(Betrag.euro("40,00"), Betrag.euro("40,00"), Betrag.euro("40,00")),
                ratenplan.raten(Betrag.euro("120,00"), 3));

        List<Betrag> raten = ratenplan.raten(Betrag.euro("23,80"), 3);

        assertEquals(3, raten.size());
        assertEquals(1, raten.stream().distinct().count());
    }

    @Test
    void anzahlDerRatenMussZwischenEinsUndZwoelfLiegen() {
        Betrag gesamt = Betrag.euro("100,00");

        assertThrows(IllegalArgumentException.class, () -> ratenplan.raten(gesamt, 0));
        assertThrows(IllegalArgumentException.class, () -> ratenplan.raten(gesamt, 13));
    }
}
