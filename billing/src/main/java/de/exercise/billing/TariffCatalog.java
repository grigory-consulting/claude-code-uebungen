package de.exercise.billing;

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
public class TariffCatalog {

    private final Map<String, Map<String, Tariff>> tariffsPerTenant = new HashMap<>();

    public void register(Tenant tenant, Tariff tariff) {
        tariffsPerTenant
                .computeIfAbsent(tenant.identifier(), k -> new LinkedHashMap<>())
                .put(tariff.name(), tariff);
    }

    public Optional<Tariff> tariff(Tenant tenant, String name) {
        return Optional.ofNullable(tariffsOf(tenant).get(name));
    }

    public List<Tariff> tariffs(Tenant tenant) {
        return tariffsOf(tenant).values().stream()
                .sorted(Comparator.comparing(Tariff::monthlyFee).thenComparing(Tariff::name))
                .toList();
    }

    public Tariff cheapest(Tenant tenant) {
        return tariffs(tenant).stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Keine Tarife für Mandant " + tenant.identifier()));
    }

    public Contract newContract(String number, Tenant tenant, String tariffName, LocalDate start) {
        Tariff tariff = tariff(tenant, tariffName)
                .orElseThrow(() -> new NoSuchElementException(
                        "Tarif %s gibt es bei Mandant %s nicht".formatted(tariffName, tenant.identifier())));
        return Contract.openEnded(number, tenant, tariff, start);
    }

    private Map<String, Tariff> tariffsOf(Tenant tenant) {
        return tariffsPerTenant.getOrDefault(tenant.identifier(), Map.of());
    }
}
