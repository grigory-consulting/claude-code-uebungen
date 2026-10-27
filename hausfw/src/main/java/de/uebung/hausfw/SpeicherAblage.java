package de.uebung.hausfw;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Ablage im Arbeitsspeicher, behält die Reihenfolge des ersten Speicherns.
 */
public class SpeicherAblage<T> implements Ablage<T> {

    private final Map<String, T> eintraege = new LinkedHashMap<>();

    @Override
    public synchronized void speichere(String id, T objekt) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(objekt, "objekt");
        eintraege.put(id, objekt);
    }

    @Override
    public synchronized Optional<T> lade(String id) {
        return Optional.ofNullable(eintraege.get(id));
    }

    @Override
    public synchronized List<T> alle() {
        return List.copyOf(eintraege.values());
    }
}
