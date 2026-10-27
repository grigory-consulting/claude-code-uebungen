package de.exercise.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.exercise.core.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TariffCatalogTest {

    private final TariffCatalog catalog = new TariffCatalog();

    @BeforeEach
    void fill() {
        catalog.register(TestData.NORDTAL, TestData.PREMIUM);
        catalog.register(TestData.NORDTAL, TestData.BASIC);
        catalog.register(TestData.NORDTAL, TestData.COMFORT);
        catalog.register(TestData.SUEDHANG, Tariff.withoutDiscount("Basis", Money.euro("18,50")));
    }

    @Test
    void tariffsAreSortedByMonthlyFee() {
        assertEquals(List.of(TestData.BASIC, TestData.COMFORT, TestData.PREMIUM), catalog.tariffs(TestData.NORDTAL));
    }

    @Test
    void sameTariffNameDiffersPerTenant() {
        assertEquals(Money.euro("20,00"), catalog.tariff(TestData.NORDTAL, "Basis").orElseThrow().monthlyFee());
        assertEquals(Money.euro("18,50"), catalog.tariff(TestData.SUEDHANG, "Basis").orElseThrow().monthlyFee());
    }

    @Test
    void cheapestTariffOfTenant() {
        assertEquals("Basis", catalog.cheapest(TestData.SUEDHANG).name());
    }

    @Test
    void tenantWithoutTariffsHasNoCheapest() {
        Tenant newTenant = new Tenant("M-99", "Neukunde Ost");

        assertTrue(catalog.tariffs(newTenant).isEmpty());
        assertThrows(NoSuchElementException.class, () -> catalog.cheapest(newTenant));
    }

    @Test
    void newContractOnlyWithKnownTariff() {
        Contract contract = catalog.newContract("V-4711", TestData.NORDTAL, "Komfort", LocalDate.of(2025, 4, 1));

        assertEquals(TestData.COMFORT, contract.tariff());
        assertTrue(contract.isOpenEnded());
        assertThrows(NoSuchElementException.class,
                () -> catalog.newContract("V-3002", TestData.SUEDHANG, "Premium", LocalDate.of(2026, 10, 1)));
    }
}
