package de.uebung.abrechnung;

import java.util.Objects;

public record Mandant(String kennung, String name) {

    public Mandant {
        Objects.requireNonNull(kennung, "kennung");
        Objects.requireNonNull(name, "name");
    }
}
