import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PreToolUse-Hook für Bash und PowerShell: blockiert git commit, wenn die zu
 * committenden Änderungen ein IBAN-Muster enthalten. Exit-Code 2 blockiert,
 * die Begründung steht auf stderr.
 *
 * Geprüft werden die gestagten Änderungen. Staged das Kommando selbst
 * (git add vor git commit im selben Aufruf, git commit -a), zusätzlich die nicht gestagten
 * Änderungen und neue, nicht ignorierte Dateien.
 *
 * Aufruf: java .claude/hooks/IbanCommitSperre.java  (JSON des Hooks auf stdin)
 */
public class IbanCommitSperre {

    static final Pattern IBAN = Pattern.compile("DE\\d{2}( ?\\d{4}){4} ?\\d{2}");
    static final Pattern GIT_COMMIT = Pattern.compile("\\bgit\\b[^;&|\\n]*\\bcommit\\b");
    static final Pattern GIT_C = Pattern.compile("\\bgit\\s+-C\\s+(\"[^\"]+\"|'[^']+'|\\S+)");
    static final Pattern STAGT_SELBST = Pattern.compile(
            "\\bgit\\b[^;&|\\n]*\\badd\\b|\\bcommit\\b[^;&|\\n]*(\\s-[A-Za-z]*a[A-Za-z]*\\b|\\s--all\\b)");

    private static final long MAX_DATEIGROESSE = 1_000_000;

    public static void main(String[] args) throws Exception {
        PrintStream stderr = new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8);
        String json = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        String befehl = JsonFeld.text(json, "command");
        if (befehl == null || !GIT_COMMIT.matcher(befehl).find()) {
            System.exit(0);
        }
        File verzeichnis = arbeitsverzeichnis(json, befehl);
        if (verzeichnis == null) {
            System.exit(0);
        }

        List<String> funde = new ArrayList<>();
        pruefeDiff(git(verzeichnis, "diff", "--cached", "--no-color", "--no-ext-diff", "-U0"), funde);
        if (STAGT_SELBST.matcher(befehl).find()) {
            pruefeDiff(git(verzeichnis, "diff", "--no-color", "--no-ext-diff", "-U0"), funde);
            pruefeNeueDateien(verzeichnis, funde);
        }
        if (funde.isEmpty()) {
            System.exit(0);
        }
        stderr.println("Commit blockiert: IBAN-Muster in den zu committenden Änderungen gefunden.");
        funde.forEach(f -> stderr.println("  " + f));
        stderr.println("Entfernen Sie die IBANs aus diesen Dateien oder nehmen Sie die Dateien aus dem Commit.");
        System.exit(2);
    }

    static void pruefeDiff(String diff, List<String> funde) {
        if (diff == null) {
            return;
        }
        String datei = "?";
        int zeile = 0;
        for (String z : diff.split("\\R")) {
            if (z.startsWith("+++ ")) {
                datei = z.startsWith("+++ b/") ? z.substring(6) : z.substring(4);
            } else if (z.startsWith("@@")) {
                Matcher m = Pattern.compile("\\+(\\d+)").matcher(z);
                zeile = m.find() ? Integer.parseInt(m.group(1)) : 0;
            } else if (z.startsWith("+")) {
                melde(datei, zeile, z.substring(1), funde);
                zeile++;
            }
        }
    }

    static void pruefeNeueDateien(File verzeichnis, List<String> funde) throws IOException, InterruptedException {
        String liste = git(verzeichnis, "ls-files", "--others", "--exclude-standard", "-z");
        if (liste == null) {
            return;
        }
        for (String name : liste.split("\0")) {
            File datei = new File(verzeichnis, name);
            if (name.isEmpty() || !datei.isFile() || datei.length() > MAX_DATEIGROESSE) {
                continue;
            }
            String inhalt = new String(Files.readAllBytes(datei.toPath()), StandardCharsets.ISO_8859_1);
            int nummer = 0;
            for (String z : inhalt.split("\\R")) {
                melde(name, ++nummer, z, funde);
            }
        }
    }

    static void melde(String datei, int zeile, String text, List<String> funde) {
        Matcher m = IBAN.matcher(text);
        while (m.find()) {
            funde.add(datei + ":" + zeile + "  " + maskiere(m.group()));
        }
    }

    static String maskiere(String iban) {
        String kompakt = iban.replace(" ", "");
        return kompakt.substring(0, 4) + " **** **** **** **** " + kompakt.substring(kompakt.length() - 2);
    }

    private static File arbeitsverzeichnis(String json, String befehl) throws IOException, InterruptedException {
        List<File> kandidaten = new ArrayList<>();
        String cwd = JsonFeld.text(json, "cwd");
        File basis = new File(cwd != null ? cwd : System.getProperty("user.dir"));
        Matcher c = GIT_C.matcher(befehl);
        if (c.find()) {
            String pfad = c.group(1).replaceAll("^[\"']|[\"']$", "");
            File ziel = new File(pfad);
            kandidaten.add(ziel.isAbsolute() ? ziel : new File(basis, pfad));
        }
        kandidaten.add(basis);
        String projekt = System.getenv("CLAUDE_PROJECT_DIR");
        if (projekt != null) {
            kandidaten.add(new File(projekt));
        }
        for (File kandidat : kandidaten) {
            String wurzel = kandidat.isDirectory() ? git(kandidat, "rev-parse", "--show-toplevel") : null;
            if (wurzel != null && !wurzel.isBlank()) {
                return new File(wurzel.strip());
            }
        }
        return null;
    }

    private static String git(File verzeichnis, String... argumente) throws IOException, InterruptedException {
        List<String> befehl = new ArrayList<>();
        befehl.add("git");
        befehl.addAll(List.of(argumente));
        Process prozess = new ProcessBuilder(befehl)
                .directory(verzeichnis)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        prozess.getOutputStream().close();
        String ausgabe = new String(prozess.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return prozess.waitFor() == 0 ? ausgabe : null;
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
