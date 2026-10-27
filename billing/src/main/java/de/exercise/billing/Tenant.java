package de.exercise.billing;

import java.util.Objects;

public record Tenant(String identifier, String name) {

    public Tenant {
        Objects.requireNonNull(identifier, "identifier");
        Objects.requireNonNull(name, "name");
    }
}
