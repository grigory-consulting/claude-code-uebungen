import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PostToolUse-Hook für Edit und Write: kompiliert das Projekt nach jeder Änderung
 * an einer .java-Datei oder pom.xml. Bei Fehlern Exit-Code 2, die Compiler-Ausgabe
 * geht über stderr an Claude.
 *
 * Aufruf: java .claude/hooks/KompilierPruefung.java  (JSON des Hooks auf stdin)
 */
public class KompilierPruefung {

    private static final int MAX_ZEILEN = 60;

    public static void main(String[] args) throws Exception {
        PrintStream stderr = new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8);
        String json = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        String datei = JsonFeld.text(json, "file_path");
        if (datei == null || !(datei.endsWith(".java") || datei.endsWith("pom.xml"))) {
            System.exit(0);
        }

        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        List<String> befehl = new ArrayList<>(windows
                ? List.of("cmd.exe", "/c", "mvnw.cmd")
                : List.of("sh", "mvnw"));
        befehl.addAll(List.of("-q", "-o", "compile"));

        Process prozess = new ProcessBuilder(befehl)
                .directory(projektverzeichnis(json))
                .redirectErrorStream(true)
                .start();
        prozess.getOutputStream().close();
        String ausgabe = new String(prozess.getInputStream().readAllBytes(), konsolenzeichensatz());
        if (prozess.waitFor() != 0) {
            stderr.println("Kompilieren nach Änderung an " + datei + " fehlgeschlagen:");
            stderr.println(letzteZeilen(ausgabe));
            System.exit(2);
        }
        System.exit(0);
    }

    private static File projektverzeichnis(String json) {
        String ausUmgebung = System.getenv("CLAUDE_PROJECT_DIR");
        if (ausUmgebung != null && new File(ausUmgebung, "pom.xml").isFile()) {
            return new File(ausUmgebung);
        }
        String cwd = JsonFeld.text(json, "cwd");
        File ort = new File(cwd != null ? cwd : System.getProperty("user.dir")).getAbsoluteFile();
        while (ort != null && !new File(ort, "mvnw").isFile()) {
            ort = ort.getParentFile();
        }
        return ort != null ? ort : new File(System.getProperty("user.dir"));
    }

    private static Charset konsolenzeichensatz() {
        String name = System.getProperty("native.encoding");
        return name != null && Charset.isSupported(name) ? Charset.forName(name) : Charset.defaultCharset();
    }

    private static String letzteZeilen(String text) {
        String[] zeilen = text.strip().split("\\R");
        int von = Math.max(0, zeilen.length - MAX_ZEILEN);
        return String.join(System.lineSeparator(), Arrays.copyOfRange(zeilen, von, zeilen.length));
    }
}

/**
 * Minimaler Leser für ein Zeichenkettenfeld aus dem Hook-JSON, ohne Bibliotheken.
 */
final class JsonFeld {

    static String text(String json, String name) {
        String schluessel = "\"" + name + "\"";
        int pos = 0;
        while ((pos = json.indexOf(schluessel, pos)) >= 0) {
            int i = pos + schluessel.length();
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                i++;
            }
            if (i < json.length() && json.charAt(i) == ':' && (pos == 0 || json.charAt(pos - 1) != '\\')) {
                i++;
                while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                    i++;
                }
                if (i < json.length() && json.charAt(i) == '"') {
                    return zeichenkette(json, i + 1);
                }
                return null;
            }
            pos = pos + 1;
        }
        return null;
    }

    private static String zeichenkette(String json, int start) {
        StringBuilder wert = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                return wert.toString();
            }
            if (c != '\\' || i + 1 >= json.length()) {
                wert.append(c);
                continue;
            }
            char e = json.charAt(++i);
            switch (e) {
                case 'n' -> wert.append('\n');
                case 't' -> wert.append('\t');
                case 'r' -> wert.append('\r');
                case 'b' -> wert.append('\b');
                case 'f' -> wert.append('\f');
                case 'u' -> {
                    wert.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                    i += 4;
                }
                default -> wert.append(e);
            }
        }
        return wert.toString();
    }
}
