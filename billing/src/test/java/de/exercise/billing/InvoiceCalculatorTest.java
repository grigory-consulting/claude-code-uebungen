package de.exercise.billing;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.exercise.core.Money;
import de.exercise.core.Configuration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class InvoiceCalculatorTest {

    private static final YearMonth SEPTEMBER = YearMonth.of(2026, 9);

    private final InvoiceCalculator calculator = new InvoiceCalculator(Configuration.standard());

    @Test
    void basicTariffWithoutDiscount() {
        Invoice invoice = calculator.calculate(TestData.basicContract(), SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("20,00"), invoice.net()),
                () -> assertEquals(Money.euro("3,80"), invoice.tax()),
                () -> assertEquals(Money.ZERO, invoice.discount()),
                () -> assertEquals(Money.euro("23,80"), invoice.gross()));
    }

    @Test
    void comfortTariffWithFifteenPercentDiscount() {
        Invoice invoice = calculator.calculate(TestData.comfortContract(), SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("39,90"), invoice.net()),
                () -> assertEquals(Money.euro("7,58"), invoice.tax()),
                () -> assertEquals(Money.euro("7,12"), invoice.discount()),
                () -> assertEquals(Money.euro("40,36"), invoice.gross()));
    }

    @Test
    void premiumTariffWithFivePercentDiscount() {
        Contract contract = TestData.contract("V-1003", TestData.PREMIUM, LocalDate.of(2024, 3, 1));

        Invoice invoice = calculator.calculate(contract, SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("11,38"), invoice.tax()),
                () -> assertEquals(Money.euro("3,56"), invoice.discount()),
                () -> assertEquals(Money.euro("67,72"), invoice.gross()));
    }

    @Test
    void dueOnFifteenthOfBillingMonth() {
        Invoice invoice = calculator.calculate(TestData.basicContract(), YearMonth.of(2027, 2));

        assertEquals(LocalDate.of(2027, 2, 15), invoice.due());
    }

    @Test
    void taxRateComesFromConfiguration() {
        Properties values = new Properties();
        values.setProperty("tax.rate.percent", "7");
        InvoiceCalculator discounted = new InvoiceCalculator(Configuration.from(values));

        Invoice invoice = discounted.calculate(TestData.basicContract(), SEPTEMBER);

        assertEquals(Money.euro("1,40"), invoice.tax());
        assertEquals(Money.euro("21,40"), invoice.gross());
    }

    @Test
    void monthBeforeContractStartIsInvalid() {
        Contract contract = TestData.contract("V-1004", TestData.BASIC, LocalDate.of(2026, 10, 1));

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(contract, SEPTEMBER));
    }

    @Test
    void monthAfterContractEndIsInvalid() {
        Contract contract = TestData.basicContract().endedOn(LocalDate.of(2026, 8, 31));

        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(contract, SEPTEMBER));
    }

    @Test
    void lastContractMonthIsBilled() {
        Contract contract = TestData.basicContract().endedOn(LocalDate.of(2026, 9, 30));

        assertEquals(Money.euro("23,80"), calculator.calculate(contract, SEPTEMBER).gross());
    }
}
