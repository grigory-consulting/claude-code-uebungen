package de.uebung.hausfw;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Einheitliches Logging für alle Module. Schreibt nach stderr.
 * Platzhalter in der Vorlage: {@code {}}.
 */
public final class HausLog {

    private static final DateTimeFormatter ZEITSTEMPEL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final String quelle;

    private HausLog(String quelle) {
        this.quelle = quelle;
    }

    public static HausLog fuer(Class<?> klasse) {
        return new HausLog(klasse.getSimpleName());
    }

    public void info(String vorlage, Object... werte) {
        schreibe("INFO", vorlage, werte);
    }

    public void warnung(String vorlage, Object... werte) {
        schreibe("WARN", vorlage, werte);
    }

    public void fehler(String vorlage, Object... werte) {
        schreibe("FEHLER", vorlage, werte);
    }

    static String fuelle(String vorlage, Object... werte) {
        StringBuilder text = new StringBuilder(vorlage.length() + 32);
        int wert = 0;
        int pos = 0;
        while (pos < vorlage.length()) {
            int platzhalter = vorlage.indexOf("{}", pos);
            if (platzhalter < 0 || wert >= werte.length) {
                text.append(vorlage, pos, vorlage.length());
                break;
            }
            text.append(vorlage, pos, platzhalter).append(werte[wert++]);
            pos = platzhalter + 2;
        }
        return text.toString();
    }

    private void schreibe(String stufe, String vorlage, Object... werte) {
        String zeile = "%s %-6s %s - %s".formatted(
                LocalDateTime.now().format(ZEITSTEMPEL), stufe, quelle, fuelle(vorlage, werte));
        System.err.println(zeile);
    }
}
