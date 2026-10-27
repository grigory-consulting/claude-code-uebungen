package de.uebung.hausfw;

import java.math.RoundingMode;

public enum Rundung {

    KAUFMAENNISCH(RoundingMode.HALF_UP),
    ABRUNDEN(RoundingMode.DOWN);

    private final RoundingMode modus;

    Rundung(RoundingMode modus) {
        this.modus = modus;
    }

    RoundingMode modus() {
        return modus;
    }
}
