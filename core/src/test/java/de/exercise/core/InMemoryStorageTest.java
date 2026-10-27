package de.exercise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InMemoryStorageTest {

    private final Storage<String> storage = new InMemoryStorage<>();

    @Test
    void savedObjectCanBeLoaded() {
        storage.save("a", "Anton");
        assertEquals(Optional.of("Anton"), storage.load("a"));
    }

    @Test
    void unknownIdReturnsEmptyOptional() {
        assertTrue(storage.load("fehlt").isEmpty());
    }

    @Test
    void savingAgainReplacesAndKeepsOrder() {
        storage.save("a", "eins");
        storage.save("b", "zwei");
        storage.save("a", "drei");
        assertEquals(List.of("drei", "zwei"), storage.all());
    }

    @Test
    void nullIsRejected() {
        assertThrows(NullPointerException.class, () -> storage.save("x", null));
    }
}
