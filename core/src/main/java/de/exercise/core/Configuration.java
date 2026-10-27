package de.exercise.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Zentrale Einstellungen aus {@code billing.properties} im Klassenpfad.
 */
public final class Configuration {

    static final String FILE = "billing.properties";

    private final Properties values;

    private Configuration(Properties values) {
        this.values = values;
    }

    public static Configuration standard() {
        try (InputStream input = Configuration.class.getClassLoader().getResourceAsStream(FILE)) {
            if (input == null) {
                throw new IllegalStateException(FILE + " nicht im Klassenpfad gefunden");
            }
            Properties values = new Properties();
            values.load(input);
            return new Configuration(values);
        } catch (IOException e) {
            throw new UncheckedIOException("Konfiguration nicht lesbar", e);
        }
    }

    public static Configuration from(Properties values) {
        Properties copy = new Properties();
        copy.putAll(values);
        return new Configuration(copy);
    }

    public BigDecimal taxRatePercent() {
        return new BigDecimal(requiredValue("tax.rate.percent"));
    }

    public Money reminderFee() {
        return Money.euro(requiredValue("reminder.fee.euro"));
    }

    public int reminderPeriodDays() {
        return Integer.parseInt(requiredValue("reminder.period.days"));
    }

    private String requiredValue(String key) {
        String value = values.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Einstellung fehlt: " + key);
        }
        return value.trim();
    }
}
