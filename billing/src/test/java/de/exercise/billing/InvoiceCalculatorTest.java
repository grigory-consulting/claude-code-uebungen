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

    private Contract basicStartingOn(LocalDate start) {
        return TestData.contract("V-2001", TestData.BASIC, start);
    }

    @Test
    void firstMonthStartingMidMonthIsBilledProportionally() {
        Invoice invoice = calculator.calculate(basicStartingOn(LocalDate.of(2026, 9, 16)), SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("10,00"), invoice.net()),
                () -> assertEquals(Money.euro("1,90"), invoice.tax()),
                () -> assertEquals(Money.euro("11,90"), invoice.gross()));
    }

    @Test
    void contractStartingOnFirstOfMonthIsBilledFully() {
        Invoice invoice = calculator.calculate(basicStartingOn(LocalDate.of(2026, 9, 1)), SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("20,00"), invoice.net()),
                () -> assertEquals(Money.euro("23,80"), invoice.gross()));
    }

    @Test
    void lastMonthEndingMidMonthIsBilledProportionally() {
        Contract contract = TestData.basicContract().endedOn(LocalDate.of(2026, 9, 15));

        Invoice invoice = calculator.calculate(contract, SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("10,00"), invoice.net()),
                () -> assertEquals(Money.euro("1,90"), invoice.tax()));
    }

    @Test
    void contractStartingAndEndingInSameMonthIsRoundedOnce() {
        Contract contract = basicStartingOn(LocalDate.of(2026, 9, 10)).endedOn(LocalDate.of(2026, 9, 20));

        Invoice invoice = calculator.calculate(contract, SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("7,33"), invoice.net()),
                () -> assertEquals(Money.euro("1,39"), invoice.tax()));
    }

    @Test
    void proportionalBillingInThirtyOneDayMonthUsesThirtyOneDays() {
        Invoice invoice = calculator.calculate(
                basicStartingOn(LocalDate.of(2026, 10, 16)), YearMonth.of(2026, 10));

        assertAll(
                () -> assertEquals(Money.euro("10,32"), invoice.net()),
                () -> assertEquals(Money.euro("1,96"), invoice.tax()));
    }

    @Test
    void proportionalBillingInFebruaryOfCommonYearUsesTwentyEightDays() {
        Invoice invoice = calculator.calculate(
                basicStartingOn(LocalDate.of(2027, 2, 15)), YearMonth.of(2027, 2));

        assertAll(
                () -> assertEquals(Money.euro("10,00"), invoice.net()),
                () -> assertEquals(Money.euro("1,90"), invoice.tax()));
    }

    @Test
    void proportionalBillingInFebruaryOfLeapYearUsesTwentyNineDays() {
        Invoice invoice = calculator.calculate(
                basicStartingOn(LocalDate.of(2028, 2, 15)), YearMonth.of(2028, 2));

        assertAll(
                () -> assertEquals(Money.euro("10,34"), invoice.net()),
                () -> assertEquals(Money.euro("1,96"), invoice.tax()));
    }

    @Test
    void contractStartingOnLastDayOfMonthIsBilledForOneDay() {
        Invoice invoice = calculator.calculate(
                basicStartingOn(LocalDate.of(2026, 8, 31)), YearMonth.of(2026, 8));

        assertAll(
                () -> assertEquals(Money.euro("0,65"), invoice.net()),
                () -> assertEquals(Money.euro("0,12"), invoice.tax()));
    }

    @Test
    void discountIsAppliedToProportionalAmount() {
        Contract contract = TestData.contract("V-2002", TestData.COMFORT, LocalDate.of(2026, 9, 16));

        Invoice invoice = calculator.calculate(contract, SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("19,95"), invoice.net()),
                () -> assertEquals(Money.euro("3,79"), invoice.tax()),
                () -> assertEquals(Money.euro("3,56"), invoice.discount()),
                () -> assertEquals(Money.euro("20,18"), invoice.gross()));
    }

    @Test
    void contractEndingOnLastDayOfFebruaryIsBilledFully() {
        Contract common = TestData.basicContract().endedOn(LocalDate.of(2027, 2, 28));
        Contract leap = TestData.basicContract().endedOn(LocalDate.of(2028, 2, 29));

        assertAll(
                () -> assertEquals(Money.euro("20,00"), calculator.calculate(common, YearMonth.of(2027, 2)).net()),
                () -> assertEquals(Money.euro("20,00"), calculator.calculate(leap, YearMonth.of(2028, 2)).net()));
    }

    @Test
    void contractStartingOnFirstAndEndingMidMonthIsBilledProportionally() {
        Contract contract = basicStartingOn(LocalDate.of(2026, 9, 1)).endedOn(LocalDate.of(2026, 9, 15));

        Invoice invoice = calculator.calculate(contract, SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("10,00"), invoice.net()),
                () -> assertEquals(Money.euro("1,90"), invoice.tax()));
    }

    @Test
    void contractStartingAndEndingOnSameDayIsBilledForOneDay() {
        LocalDate day = LocalDate.of(2026, 9, 10);

        Invoice invoice = calculator.calculate(basicStartingOn(day).endedOn(day), SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("0,67"), invoice.net()),
                () -> assertEquals(Money.euro("0,13"), invoice.tax()));
    }

    @Test
    void monthBeforeLaterEndMonthIsBilledFully() {
        Contract contract = TestData.basicContract().endedOn(LocalDate.of(2026, 10, 15));

        Invoice invoice = calculator.calculate(contract, SEPTEMBER);

        assertEquals(Money.euro("20,00"), invoice.net());
    }

    @Test
    void monthAfterStartMonthIsBilledFully() {
        Invoice invoice = calculator.calculate(
                basicStartingOn(LocalDate.of(2026, 9, 16)), YearMonth.of(2026, 10));

        assertAll(
                () -> assertEquals(Money.euro("20,00"), invoice.net()),
                () -> assertEquals(Money.euro("23,80"), invoice.gross()));
    }

    @Test
    void halfCentIsRoundedUp() {
        Contract contract = TestData.contract("V-2003", TestData.PREMIUM, LocalDate.of(2027, 2, 22));

        Invoice invoice = calculator.calculate(contract, YearMonth.of(2027, 2));

        assertEquals(Money.euro("14,98"), invoice.net());
    }

    @Test
    void contractStartingInDecemberIsBilledForRemainingDays() {
        Invoice invoice = calculator.calculate(
                basicStartingOn(LocalDate.of(2026, 12, 20)), YearMonth.of(2026, 12));

        assertEquals(Money.euro("7,74"), invoice.net());
    }

    @Test
    void contractEndingInJanuaryIsBilledForDaysUntilEnd() {
        Contract contract = TestData.basicContract().endedOn(LocalDate.of(2027, 1, 10));

        Invoice invoice = calculator.calculate(contract, YearMonth.of(2027, 1));

        assertEquals(Money.euro("6,45"), invoice.net());
    }

    @Test
    void contractEndingOnFirstOfMonthIsBilledForOneDay() {
        Contract contract = TestData.basicContract().endedOn(LocalDate.of(2026, 9, 1));

        Invoice invoice = calculator.calculate(contract, SEPTEMBER);

        assertAll(
                () -> assertEquals(Money.euro("0,67"), invoice.net()),
                () -> assertEquals(Money.euro("0,13"), invoice.tax()));
    }
}
