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
 * Aufruf: java .claude/hooks/IbanCommitBlock.java  (JSON des Hooks auf stdin)
 */
public class IbanCommitBlock {

    static final Pattern IBAN = Pattern.compile("DE\\d{2}( ?\\d{4}){4} ?\\d{2}");
    static final Pattern GIT_COMMIT = Pattern.compile("\\bgit\\b[^;&|\\n]*\\bcommit\\b");
    static final Pattern GIT_C = Pattern.compile("\\bgit\\s+-C\\s+(\"[^\"]+\"|'[^']+'|\\S+)");
    static final Pattern STAGES_ITSELF = Pattern.compile(
            "\\bgit\\b[^;&|\\n]*\\badd\\b|\\bcommit\\b[^;&|\\n]*(\\s-[A-Za-z]*a[A-Za-z]*\\b|\\s--all\\b)");

    private static final long MAX_FILE_SIZE = 1_000_000;

    public static void main(String[] args) throws Exception {
        PrintStream stderr = new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8);
        String json = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
        String command = JsonField.text(json, "command");
        if (command == null || !GIT_COMMIT.matcher(command).find()) {
            System.exit(0);
        }
        File directory = workingDir(json, command);
        if (directory == null) {
            System.exit(0);
        }

        List<String> findings = new ArrayList<>();
        checkDiff(git(directory, "diff", "--cached", "--no-color", "--no-ext-diff", "-U0"), findings);
        if (STAGES_ITSELF.matcher(command).find()) {
            checkDiff(git(directory, "diff", "--no-color", "--no-ext-diff", "-U0"), findings);
            checkNewFiles(directory, findings);
        }
        if (findings.isEmpty()) {
            System.exit(0);
        }
        stderr.println("Commit blockiert: IBAN-Muster in den zu committenden Änderungen gefunden.");
        findings.forEach(f -> stderr.println("  " + f));
        stderr.println("Entfernen Sie die IBANs aus diesen Dateien oder nehmen Sie die Dateien aus dem Commit.");
        System.exit(2);
    }

    static void checkDiff(String diff, List<String> findings) {
        if (diff == null) {
            return;
        }
        String file = "?";
        int line = 0;
        for (String z : diff.split("\\R")) {
            if (z.startsWith("+++ ")) {
                file = z.startsWith("+++ b/") ? z.substring(6) : z.substring(4);
            } else if (z.startsWith("@@")) {
                Matcher m = Pattern.compile("\\+(\\d+)").matcher(z);
                line = m.find() ? Integer.parseInt(m.group(1)) : 0;
            } else if (z.startsWith("+")) {
                report(file, line, z.substring(1), findings);
                line++;
            }
        }
    }

    static void checkNewFiles(File directory, List<String> findings) throws IOException, InterruptedException {
        String list = git(directory, "ls-files", "--others", "--exclude-standard", "-z");
        if (list == null) {
            return;
        }
        for (String name : list.split("\0")) {
            File file = new File(directory, name);
            if (name.isEmpty() || !file.isFile() || file.length() > MAX_FILE_SIZE) {
                continue;
            }
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);
            int number = 0;
            for (String z : content.split("\\R")) {
                report(name, ++number, z, findings);
            }
        }
    }

    static void report(String file, int line, String text, List<String> findings) {
        Matcher m = IBAN.matcher(text);
        while (m.find()) {
            findings.add(file + ":" + line + "  " + mask(m.group()));
        }
    }

    static String mask(String iban) {
        String compact = iban.replace(" ", "");
        return compact.substring(0, 4) + " **** **** **** **** " + compact.substring(compact.length() - 2);
    }

    private static File workingDir(String json, String command) throws IOException, InterruptedException {
        List<File> candidates = new ArrayList<>();
        String cwd = JsonField.text(json, "cwd");
        File base = new File(cwd != null ? cwd : System.getProperty("user.dir"));
        Matcher c = GIT_C.matcher(command);
        if (c.find()) {
            String path = c.group(1).replaceAll("^[\"']|[\"']$", "");
            File target = new File(path);
            candidates.add(target.isAbsolute() ? target : new File(base, path));
        }
        candidates.add(base);
        String project = System.getenv("CLAUDE_PROJECT_DIR");
        if (project != null) {
            candidates.add(new File(project));
        }
        for (File candidate : candidates) {
            String root = candidate.isDirectory() ? git(candidate, "rev-parse", "--show-toplevel") : null;
            if (root != null && !root.isBlank()) {
                return new File(root.strip());
            }
        }
        return null;
    }

    private static String git(File directory, String... arguments) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command)
                .directory(directory)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        process.getOutputStream().close();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return process.waitFor() == 0 ? output : null;
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
