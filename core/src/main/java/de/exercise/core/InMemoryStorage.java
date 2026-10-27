package de.exercise.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Ablage im Arbeitsspeicher, behält die Reihenfolge des ersten Speicherns.
 */
public class InMemoryStorage<T> implements Storage<T> {

    private final Map<String, T> entries = new LinkedHashMap<>();

    @Override
    public synchronized void save(String id, T object) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(object, "object");
        entries.put(id, object);
    }

    @Override
    public synchronized Optional<T> load(String id) {
        return Optional.ofNullable(entries.get(id));
    }

    @Override
    public synchronized List<T> all() {
        return List.copyOf(entries.values());
    }
}
