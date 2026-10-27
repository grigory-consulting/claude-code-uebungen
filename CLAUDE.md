# Vertragsabrechnung

Java 21, Maven Multi-Module, JUnit 5.

## Bauen und testen

- PowerShell: `.\mvnw.cmd test`, Eingabeaufforderung: `mvnw.cmd test`
- Git Bash, macOS, Linux: `./mvnw test`
- Ein Modul: `-pl billing -am` anhängen.
- Einzelner Test, PowerShell: `.\mvnw.cmd test -pl billing -am -Dtest=InstallmentPlanTest "-Dsurefire.failIfNoSpecifiedTests=false"`
- Einzelner Test, Git Bash, macOS, Linux: `./mvnw test -pl billing -am -Dtest=InstallmentPlanTest "-Dsurefire.failIfNoSpecifiedTests=false"`
- Die Anführungszeichen um `-Dsurefire…` sind für PowerShell nötig, sonst trennt sie das Argument am Punkt.
- Immer den Wrapper benutzen, nie ein lokal installiertes `mvn`.
- Eine Änderung ist erst fertig, wenn `mvnw test` grün ist.

## Module

- `core`: Hausframework. Nur mit Rückfrage ändern.
- `billing`: Fachcode, hängt nur von `core` ab.
- `legacy`: Altcode, hängt von nichts ab. Verhalten erst mit Tests sichern, dann ändern.
- Keine neuen Maven-Abhängigkeiten ohne Rückfrage.

## Regeln, die immer gelten

- Geld nur als `de.exercise.core.Money`. Nie `double`, `float` oder rohes `BigDecimal` für Beträge.
- Runden nur mit `de.exercise.core.Rounding` über die Methoden von `Money`.
- Logging nur mit `CoreLog.forClass(...)`. Kein `System.out`, kein `java.util.logging`, kein SLF4J.
- Keine personenbezogenen Daten ins Log: keine Namen, IBANs oder Adressen. Vertragsnummer, Monat und Betrag sind erlaubt.
- Steuersatz, Fristen und Gebühren kommen aus `Configuration`, nie als Konstante in den Fachcode.
- Tests: JUnit 5, Testnamen auf Englisch und sprechend.

## Teamstandards

@docs/team-standards.md
