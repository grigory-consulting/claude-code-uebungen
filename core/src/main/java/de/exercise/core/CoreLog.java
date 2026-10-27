package de.exercise.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Einheitliches Logging für alle Module. Schreibt nach stderr.
 * Platzhalter in der Vorlage: {@code {}}.
 */
public final class CoreLog {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final String source;

    private CoreLog(String source) {
        this.source = source;
    }

    public static CoreLog forClass(Class<?> clazz) {
        return new CoreLog(clazz.getSimpleName());
    }

    public void info(String template, Object... values) {
        write("INFO", template, values);
    }

    public void warn(String template, Object... values) {
        write("WARN", template, values);
    }

    public void error(String template, Object... values) {
        write("FEHLER", template, values);
    }

    static String fill(String template, Object... values) {
        StringBuilder text = new StringBuilder(template.length() + 32);
        int value = 0;
        int pos = 0;
        while (pos < template.length()) {
            int placeholder = template.indexOf("{}", pos);
            if (placeholder < 0 || value >= values.length) {
                text.append(template, pos, template.length());
                break;
            }
            text.append(template, pos, placeholder).append(values[value++]);
            pos = placeholder + 2;
        }
        return text.toString();
    }

    private void write(String level, String template, Object... values) {
        String line = "%s %-6s %s - %s".formatted(
                LocalDateTime.now().format(TIMESTAMP), level, source, fill(template, values));
        System.err.println(line);
    }
}
