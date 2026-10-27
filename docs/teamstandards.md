# Teamstandards

Gilt für allen Fachcode in `abrechnung` und für jeden neuen Code in `altbestand`.

## Geld und Rundung

- Geldbeträge sind immer `de.uebung.hausfw.Betrag`. Kein `double`, kein `float`, kein rohes `BigDecimal` für Geld, auch nicht als Zwischenwert.
- Gerundet wird nur über `de.uebung.hausfw.Rundung` (`KAUFMAENNISCH` oder `ABRUNDEN`), nie über `RoundingMode` oder `Math.round`.
- Faktoren und Prozentsätze dürfen `BigDecimal` sein (z. B. `rabattProzent`), Beträge nicht.

## Logging

- Logging nur über `de.uebung.hausfw.HausLog` (`HausLog.fuer(Klasse.class)`).
- Kein `System.out`, kein `java.util.logging`, kein SLF4J.
- Keine personenbezogenen Daten ins Log: keine Namen, keine IBANs, keine Adressen. Erlaubt sind Vertragsnummern, Monate und Beträge.

## Konfiguration und Ablage

- Einstellungen nur über `Konfiguration`, keine fest verdrahteten Steuersätze oder Fristen im Fachcode.
- Daten nur über `Ablage<T>` speichern und laden.

## Tests

- JUnit 5 (Jupiter), sonst keine Testbibliothek.
- Testnamen auf Deutsch und sprechend, z. B. `ersteMahnungAmTagNachFristende`.
- Jeder Fehler bekommt zuerst einen Test, der ihn zeigt.

## Build

- Bauen und testen mit dem Maven Wrapper: `mvnw.cmd test` unter Windows, `./mvnw test` unter macOS und Linux.
- Keine neuen Abhängigkeiten ohne Rückfrage im Team.

## Code

- Bezeichner auf Deutsch, Umlaute ausgeschrieben (`pruefe`, `Saeumnis`).
- Java 21: Records für Datenklassen, `java.time` für Datumswerte, `Optional` statt `null` als Rückgabe.
