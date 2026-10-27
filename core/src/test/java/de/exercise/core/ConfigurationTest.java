package de.exercise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ConfigurationTest {

    @Test
    void standardReadsFileFromClasspath() {
        Configuration configuration = Configuration.standard();
        assertEquals(new BigDecimal("7"), configuration.taxRatePercent());
        assertEquals(Money.euro("2,50"), configuration.reminderFee());
        assertEquals(10, configuration.reminderPeriodDays());
    }

    @Test
    void missingKeyGivesClearMessage() {
        Properties values = new Properties();
        values.setProperty("tax.rate.percent", "19");
        Configuration configuration = Configuration.from(values);

        IllegalStateException error = assertThrows(IllegalStateException.class, configuration::reminderPeriodDays);
        assertEquals("Einstellung fehlt: reminder.period.days", error.getMessage());
    }

    @Test
    void changesToSourceValuesDoNotCarryOver() {
        Properties values = new Properties();
        values.setProperty("reminder.period.days", "14");
        Configuration configuration = Configuration.from(values);
        values.setProperty("reminder.period.days", "1");

        assertEquals(14, configuration.reminderPeriodDays());
    }
}
