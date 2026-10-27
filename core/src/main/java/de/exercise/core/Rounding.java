package de.exercise.core;

import java.math.RoundingMode;

public enum Rounding {

    HALF_UP(RoundingMode.HALF_UP),
    DOWN(RoundingMode.DOWN);

    private final RoundingMode mode;

    Rounding(RoundingMode mode) {
        this.mode = mode;
    }

    RoundingMode mode() {
        return mode;
    }
}
