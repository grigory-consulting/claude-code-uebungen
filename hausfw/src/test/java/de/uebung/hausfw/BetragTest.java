package de.uebung.hausfw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BetragTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "12,34 | 1234",
            "12.34 | 1234",
            "-5    | -500",
            "0,5   | 50",
            "1000  | 100000"
    })
    void euroLiestKommaUndPunkt(String text, long erwarteteCent) {
        assertEquals(erwarteteCent, Betrag.euro(text).inCent());
    }

    @Test
    void euroLehntDreiNachkommastellenAb() {
        assertThrows(IllegalArgumentException.class, () -> Betrag.euro("1,005"));
    }

    @Test
    void euroLehntTextOhneZahlAb() {
        assertThrows(IllegalArgumentException.class, () -> Betrag.euro("zwölf"));
    }

    @Test
    void nullCentLiefertDieKonstante() {
        assertSame(Betrag.NULL, Betrag.cent(0));
    }

    @Test
    void plusUndMinusRechnenInCent() {
        Betrag a = Betrag.euro("10,10");
        Betrag b = Betrag.euro("0,95");
        assertEquals(Betrag.euro("11,05"), a.plus(b));
        assertEquals(Betrag.euro("9,15"), a.minus(b));
    }

    @Test
    void malRundetKaufmaennisch() {
        Betrag betrag = Betrag.euro("10,05");
        assertEquals(Betrag.euro("5,03"), betrag.mal(new BigDecimal("0.5"), Rundung.KAUFMAENNISCH));
        assertEquals(Betrag.euro("5,02"), betrag.mal(new BigDecimal("0.5"), Rundung.ABRUNDEN));
    }

    @Test
    void prozentVonNeunundvierzigNeunzig() {
        Betrag betrag = Betrag.euro("49,90");
        assertEquals(Betrag.euro("9,48"), betrag.prozent(new BigDecimal("19"), Rundung.KAUFMAENNISCH));
        assertEquals(Betrag.euro("9,48"), betrag.prozent(new BigDecimal("19"), Rundung.ABRUNDEN));
        assertEquals(Betrag.euro("4,99"), betrag.prozent(new BigDecimal("10"), Rundung.KAUFMAENNISCH));
    }

    @Test
    void prozentMitHalbemCentRundetAuf() {
        Betrag betrag = Betrag.euro("0,50");
        assertEquals(Betrag.cent(1), betrag.prozent(new BigDecimal("1"), Rundung.KAUFMAENNISCH));
        assertEquals(Betrag.NULL, betrag.prozent(new BigDecimal("1"), Rundung.ABRUNDEN));
    }

    @Test
    void geteiltDurchVierGehtAuf() {
        assertEquals(Betrag.euro("25,00"), Betrag.euro("100,00").geteilt(4, Rundung.KAUFMAENNISCH));
    }

    @Test
    void geteiltMitRestRundetNachVorgabe() {
        Betrag betrag = Betrag.euro("10,00");
        assertEquals(Betrag.euro("1,43"), betrag.geteilt(7, Rundung.KAUFMAENNISCH));
        assertEquals(Betrag.euro("1,42"), betrag.geteilt(7, Rundung.ABRUNDEN));
    }

    @Test
    void geteiltDurchNullIstUnzulaessig() {
        assertThrows(IllegalArgumentException.class, () -> Betrag.euro("1,00").geteilt(0, Rundung.KAUFMAENNISCH));
    }

    @Test
    void negativeBetraegeWerdenErkannt() {
        assertTrue(Betrag.euro("-0,01").istNegativ());
        assertFalse(Betrag.NULL.istNegativ());
    }

    @Test
    void textMitZweiNachkommastellenUndWaehrung() {
        assertEquals("23,80 EUR", Betrag.euro("23,8").toString());
        assertEquals("-0,05 EUR", Betrag.cent(-5).toString());
        assertEquals("1234,00 EUR", Betrag.euro("1234").toString());
    }

    @Test
    void gleicheCentSindGleicheBetraege() {
        assertEquals(Betrag.euro("7,00"), Betrag.cent(700));
        assertEquals(Betrag.euro("7,00").hashCode(), Betrag.cent(700).hashCode());
    }

    @Test
    void sortiertNachWert() {
        List<Betrag> betraege = new ArrayList<>(List.of(Betrag.euro("3"), Betrag.euro("-1"), Betrag.euro("2")));
        Collections.sort(betraege);
        assertEquals(List.of(Betrag.euro("-1"), Betrag.euro("2"), Betrag.euro("3")), betraege);
    }
}
