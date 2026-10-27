---
name: billing-review
description: Prüft die eigenen Änderungen an core, billing und legacy gegen die Teamstandards (Geld nur als Money, Rundung, CoreLog, keine personenbezogenen Daten im Log, Tests). Verwenden vor Commit oder Pull Request, oder wenn nach einem Review der Änderungen gefragt wird.
argument-hint: "[Basis, Standard: Starttag des aktuellen Stands]"
allowed-tools: Read Grep Glob Bash(git diff *) Bash(git log *) Bash(git describe *) Bash(git merge-base *) PowerShell(git diff *) PowerShell(git log *) PowerShell(git describe *) PowerShell(git merge-base *)
---

# Review gegen die Teamstandards

Geprüft werden nur Dateien unter `core/`, `billing/` und `legacy/`. Änderungen an `.claude/`, `CLAUDE.md`, `docs/`, `data/` und anderen Dateien gehören nicht zu diesem Review.

## Arbeitsstand

!`git status --short -- core billing legacy`

## Noch nicht committete Änderungen

!`git diff HEAD -- core billing legacy`

## Auftrag

- Basis festlegen. Argument: `$ARGUMENTS`. Ist ein Argument angegeben, ist es die Basis. Ohne Argument ist die Basis der Starttag des aktuellen Stands: `git describe --tags --abbrev=0 --match "lab*-start"`. Liefert der Befehl keinen Tag, nehmen Sie `git merge-base HEAD main`. Nennen Sie die Basis in der ersten Zeile der Ausgabe.
- Holen Sie die Commits seit der Basis: `git log --oneline <base>..HEAD -- core billing legacy` und `git diff <base>...HEAD -- core billing legacy`.
- Neue, noch nicht versionierte Dateien (`??` im Arbeitsstand oben) lesen Sie vollständig; jede ihrer Zeilen gilt als neu.
- Prüfen Sie die Diffs gegen `docs/team-standards.md` (lesen Sie die Datei).

Prüfen Sie nur geänderte oder neue Zeilen, und zwar genau diese Regeln:

1. Geldbeträge: `double`, `float`, `Double`, `Float` oder `BigDecimal` als Betrag (Feld, Parameter, Rückgabe, Zwischenwert). Erlaubt ist `BigDecimal` nur für Faktoren und Prozentsätze.
2. Rundung: `RoundingMode`, `Math.round`, `setScale` im Fachcode statt `Rounding` über `Money`.
3. Logging: `System.out`, `System.err`, `printStackTrace`, `java.util.logging`, SLF4J statt `CoreLog`.
4. Personenbezogene Daten im Log: Namen (`tenant().name()`, Kundennamen), IBANs, Adressen in `CoreLog`-Aufrufen oder Exception-Texten.
5. Konfiguration: fest verdrahtete Steuersätze, Fristen oder Gebühren statt `Configuration`.
6. Tests: Gibt es zu jeder fachlichen Änderung einen JUnit-5-Test mit englischem, sprechendem Namen? Wurden Erwartungswerte bestehender Tests geändert? Wenn ja, nennen Sie jede Stelle mit altem und neuem Wert.
7. Abhängigkeiten: neue Einträge in einer `pom.xml`.

## Ausgabe

Eine Liste, ein Punkt je Befund, sortiert nach Schwere:

- `[Muss]` oder `[Sollte]`, Datei:Zeile, Regel (Nummer oben), ein Satz zum Befund, ein Satz zur Abhilfe.

Danach eine Zeile `Ergebnis: <n> Muss, <m> Sollte`. Ohne Befunde nur `Ergebnis: keine Befunde`. Ändern Sie keine Dateien.
