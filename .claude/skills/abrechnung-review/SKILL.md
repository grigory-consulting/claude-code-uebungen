---
name: abrechnung-review
description: Prüft die eigenen Änderungen an hausfw, abrechnung und altbestand gegen die Teamstandards (Geld nur als Betrag, Rundung, HausLog, keine personenbezogenen Daten im Log, Tests). Verwenden vor Commit oder Pull Request, oder wenn nach einem Review der Änderungen gefragt wird.
argument-hint: "[Basis, Standard: Starttag des aktuellen Stands]"
allowed-tools: Read Grep Glob Bash(git diff *) Bash(git log *) Bash(git describe *) Bash(git merge-base *) PowerShell(git diff *) PowerShell(git log *) PowerShell(git describe *) PowerShell(git merge-base *)
---

# Review gegen die Teamstandards

Geprüft werden nur Dateien unter `hausfw/`, `abrechnung/` und `altbestand/`. Änderungen an `.claude/`, `CLAUDE.md`, `docs/`, `daten/` und anderen Dateien gehören nicht zu diesem Review.

## Arbeitsstand

!`git status --short -- hausfw abrechnung altbestand`

## Noch nicht committete Änderungen

!`git diff HEAD -- hausfw abrechnung altbestand`

## Auftrag

- Basis festlegen. Argument: `$ARGUMENTS`. Ist ein Argument angegeben, ist es die Basis. Ohne Argument ist die Basis der Starttag des aktuellen Stands: `git describe --tags --abbrev=0 --match "lab*-start"`. Liefert der Befehl keinen Tag, nehmen Sie `git merge-base HEAD main`. Nennen Sie die Basis in der ersten Zeile der Ausgabe.
- Holen Sie die Commits seit der Basis: `git log --oneline <basis>..HEAD -- hausfw abrechnung altbestand` und `git diff <basis>...HEAD -- hausfw abrechnung altbestand`.
- Neue, noch nicht versionierte Dateien (`??` im Arbeitsstand oben) lesen Sie vollständig; jede ihrer Zeilen gilt als neu.
- Prüfen Sie die Diffs gegen `docs/teamstandards.md` (lesen Sie die Datei).

Prüfen Sie nur geänderte oder neue Zeilen, und zwar genau diese Regeln:

1. Geldbeträge: `double`, `float`, `Double`, `Float` oder `BigDecimal` als Betrag (Feld, Parameter, Rückgabe, Zwischenwert). Erlaubt ist `BigDecimal` nur für Faktoren und Prozentsätze.
2. Rundung: `RoundingMode`, `Math.round`, `setScale` im Fachcode statt `Rundung` über `Betrag`.
3. Logging: `System.out`, `System.err`, `printStackTrace`, `java.util.logging`, SLF4J statt `HausLog`.
4. Personenbezogene Daten im Log: Namen (`mandant().name()`, Kundennamen), IBANs, Adressen in `HausLog`-Aufrufen oder Exception-Texten.
5. Konfiguration: fest verdrahtete Steuersätze, Fristen oder Gebühren statt `Konfiguration`.
6. Tests: Gibt es zu jeder fachlichen Änderung einen JUnit-5-Test mit deutschem, sprechendem Namen? Wurden Erwartungswerte bestehender Tests geändert? Wenn ja, nennen Sie jede Stelle mit altem und neuem Wert.
7. Abhängigkeiten: neue Einträge in einer `pom.xml`.

## Ausgabe

Eine Liste, ein Punkt je Befund, sortiert nach Schwere:

- `[Muss]` oder `[Sollte]`, Datei:Zeile, Regel (Nummer oben), ein Satz zum Befund, ein Satz zur Abhilfe.

Danach eine Zeile `Ergebnis: <n> Muss, <m> Sollte`. Ohne Befunde nur `Ergebnis: keine Befunde`. Ändern Sie keine Dateien.
