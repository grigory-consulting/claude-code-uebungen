# Vertragsabrechnung

Java 21, Maven Multi-Module, JUnit 5.

## Bauen und testen

- PowerShell: `.\mvnw.cmd test`, Eingabeaufforderung: `mvnw.cmd test`
- Git Bash, macOS, Linux: `./mvnw test`
- Ein Modul: `-pl abrechnung -am` anhängen.
- Einzelner Test, PowerShell: `.\mvnw.cmd test -pl abrechnung -am -Dtest=RatenplanTest "-Dsurefire.failIfNoSpecifiedTests=false"`
- Einzelner Test, Git Bash, macOS, Linux: `./mvnw test -pl abrechnung -am -Dtest=RatenplanTest "-Dsurefire.failIfNoSpecifiedTests=false"`
- Die Anführungszeichen um `-Dsurefire…` sind für PowerShell nötig, sonst trennt sie das Argument am Punkt.
- Immer den Wrapper benutzen, nie ein lokal installiertes `mvn`.
- Eine Änderung ist erst fertig, wenn `mvnw test` grün ist.

## Module

- `hausfw`: Hausframework. Nur mit Rückfrage ändern.
- `abrechnung`: Fachcode, hängt nur von `hausfw` ab.
- `altbestand`: Altcode, hängt von nichts ab. Verhalten erst mit Tests sichern, dann ändern.
- Keine neuen Maven-Abhängigkeiten ohne Rückfrage.

## Regeln, die immer gelten

- Geld nur als `de.uebung.hausfw.Betrag`. Nie `double`, `float` oder rohes `BigDecimal` für Beträge.
- Runden nur mit `de.uebung.hausfw.Rundung` über die Methoden von `Betrag`.
- Logging nur mit `HausLog.fuer(...)`. Kein `System.out`, kein `java.util.logging`, kein SLF4J.
- Keine personenbezogenen Daten ins Log: keine Namen, IBANs oder Adressen. Vertragsnummer, Monat und Betrag sind erlaubt.
- Steuersatz, Fristen und Gebühren kommen aus `Konfiguration`, nie als Konstante in den Fachcode.
- Tests: JUnit 5, Testnamen auf Deutsch und sprechend.

## Teamstandards

@docs/teamstandards.md
