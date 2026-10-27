package de.exercise.core;

import java.util.List;
import java.util.Optional;

public interface Storage<T> {

    void save(String id, T object);

    Optional<T> load(String id);

    List<T> all();
}
