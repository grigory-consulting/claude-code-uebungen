package de.uebung.hausfw;

import java.util.List;
import java.util.Optional;

public interface Ablage<T> {

    void speichere(String id, T objekt);

    Optional<T> lade(String id);

    List<T> alle();
}
