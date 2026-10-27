package de.uebung.abrechnung;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Tarife je Mandant. Tarifnamen sind je Mandant eindeutig.
 */
public class Tarifkatalog {

    private final Map<String, Map<String, Tarif>> tarifeJeMandant = new HashMap<>();

    public void hinterlege(Mandant mandant, Tarif tarif) {
        tarifeJeMandant
                .computeIfAbsent(mandant.kennung(), k -> new LinkedHashMap<>())
                .put(tarif.name(), tarif);
    }

    public Optional<Tarif> tarif(Mandant mandant, String name) {
        return Optional.ofNullable(tarifeDes(mandant).get(name));
    }

    public List<Tarif> tarife(Mandant mandant) {
        return tarifeDes(mandant).values().stream()
                .sorted(Comparator.comparing(Tarif::monatsbeitrag).thenComparing(Tarif::name))
                .toList();
    }

    public Tarif guenstigster(Mandant mandant) {
        return tarife(mandant).stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Keine Tarife für Mandant " + mandant.kennung()));
    }

    public Vertrag neuerVertrag(String nummer, Mandant mandant, String tarifname, LocalDate beginn) {
        Tarif tarif = tarif(mandant, tarifname)
                .orElseThrow(() -> new NoSuchElementException(
                        "Tarif %s gibt es bei Mandant %s nicht".formatted(tarifname, mandant.kennung())));
        return Vertrag.unbefristet(nummer, mandant, tarif, beginn);
    }

    private Map<String, Tarif> tarifeDes(Mandant mandant) {
        return tarifeJeMandant.getOrDefault(mandant.kennung(), Map.of());
    }
}
