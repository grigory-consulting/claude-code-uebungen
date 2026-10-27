package de.exercise.billing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.exercise.core.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class ContractTest {

    @Test
    void runsFromStartMonthToEndMonth() {
        Contract contract = TestData.contract("V-2001", TestData.BASIC, LocalDate.of(2026, 3, 20))
                .endedOn(LocalDate.of(2026, 6, 10));

        assertFalse(contract.runsIn(YearMonth.of(2026, 2)));
        assertTrue(contract.runsIn(YearMonth.of(2026, 3)));
        assertTrue(contract.runsIn(YearMonth.of(2026, 6)));
        assertFalse(contract.runsIn(YearMonth.of(2026, 7)));
    }

    @Test
    void openEndedContractKeepsRunning() {
        Contract contract = TestData.basicContract();

        assertTrue(contract.isOpenEnded());
        assertTrue(contract.runsIn(YearMonth.of(2040, 1)));
    }

    @Test
    void endBeforeStartIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Contract("V-2002", TestData.NORDTAL,
                TestData.BASIC, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 4, 30)));
    }

    @Test
    void discountOverHundredPercentIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> new Tariff("Falsch", Money.euro("10"), new BigDecimal("100.01")));
    }
}
