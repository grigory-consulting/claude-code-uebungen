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
 * Aufruf: java .claude/hooks/CompileCheck.java  (JSON des Hooks auf stdin)
 */
public class CompileCheck {

    private static final int MAX_LINES = 60;

    public static void main(String[] args) throws Exception {
        PrintStream stderr = new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8);
        String json = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        String file = JsonField.text(json, "file_path");
        if (file == null || !(file.endsWith(".java") || file.endsWith("pom.xml"))) {
            System.exit(0);
        }

        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        List<String> command = new ArrayList<>(windows
                ? List.of("cmd.exe", "/c", ".\\mvnw.cmd")
                : List.of("sh", "mvnw"));
        command.addAll(List.of("-q", "-o", "compile"));

        Process process = new ProcessBuilder(command)
                .directory(projectDir(json))
                .redirectErrorStream(true)
                .start();
        process.getOutputStream().close();
        String output = new String(process.getInputStream().readAllBytes(), consoleCharset());
        if (process.waitFor() != 0) {
            stderr.println("Kompilieren nach Änderung an " + file + " fehlgeschlagen:");
            stderr.println(lastLines(output));
            System.exit(2);
        }
        System.exit(0);
    }

    private static File projectDir(String json) {
        String fromEnvironment = System.getenv("CLAUDE_PROJECT_DIR");
        if (fromEnvironment != null && new File(fromEnvironment, "pom.xml").isFile()) {
            return new File(fromEnvironment);
        }
        String cwd = JsonField.text(json, "cwd");
        File location = new File(cwd != null ? cwd : System.getProperty("user.dir")).getAbsoluteFile();
        while (location != null && !new File(location, "mvnw").isFile()) {
            location = location.getParentFile();
        }
        return location != null ? location : new File(System.getProperty("user.dir"));
    }

    private static Charset consoleCharset() {
        String name = System.getProperty("native.encoding");
        return name != null && Charset.isSupported(name) ? Charset.forName(name) : Charset.defaultCharset();
    }

    private static String lastLines(String text) {
        String[] lines = text.strip().split("\\R");
        int fromIndex = Math.max(0, lines.length - MAX_LINES);
        return String.join(System.lineSeparator(), Arrays.copyOfRange(lines, fromIndex, lines.length));
    }
}

/**
 * Minimaler Leser für ein Zeichenkettenfeld aus dem Hook-JSON, ohne Bibliotheken.
 */
final class JsonField {

    static String text(String json, String name) {
        String key = "\"" + name + "\"";
        int pos = 0;
        while ((pos = json.indexOf(key, pos)) >= 0) {
            int i = pos + key.length();
            while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                i++;
            }
            if (i < json.length() && json.charAt(i) == ':' && (pos == 0 || json.charAt(pos - 1) != '\\')) {
                i++;
                while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
                    i++;
                }
                if (i < json.length() && json.charAt(i) == '"') {
                    return string(json, i + 1);
                }
                return null;
            }
            pos = pos + 1;
        }
        return null;
    }

    private static String string(String json, int start) {
        StringBuilder value = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                return value.toString();
            }
            if (c != '\\' || i + 1 >= json.length()) {
                value.append(c);
                continue;
            }
            char e = json.charAt(++i);
            switch (e) {
                case 'n' -> value.append('\n');
                case 't' -> value.append('\t');
                case 'r' -> value.append('\r');
                case 'b' -> value.append('\b');
                case 'f' -> value.append('\f');
                case 'u' -> {
                    value.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                    i += 4;
                }
                default -> value.append(e);
            }
        }
        return value.toString();
    }
}
