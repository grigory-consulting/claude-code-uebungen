# Teamstandards

Gilt für allen Fachcode in `billing` und für jeden neuen Code in `legacy`.

## Geld und Rundung

- Geldbeträge sind immer `de.exercise.core.Money`. Kein `double`, kein `float`, kein rohes `BigDecimal` für Geld, auch nicht als Zwischenwert.
- Gerundet wird nur über `de.exercise.core.Rounding` (`HALF_UP` oder `DOWN`), nie über `RoundingMode` oder `Math.round`.
- Faktoren und Prozentsätze dürfen `BigDecimal` sein (z. B. `discountPercent`), Beträge nicht.

## Logging

- Logging nur über `de.exercise.core.CoreLog` (`CoreLog.forClass(MyClass.class)`).
- Kein `System.out`, kein `java.util.logging`, kein SLF4J.
- Keine personenbezogenen Daten ins Log: keine Namen, keine IBANs, keine Adressen. Erlaubt sind Vertragsnummern, Monate und Beträge.

## Konfiguration und Ablage

- Einstellungen nur über `Configuration`, keine fest verdrahteten Steuersätze oder Fristen im Fachcode.
- Daten nur über `Storage<T>` speichern und laden.

## Tests

- JUnit 5 (Jupiter), sonst keine Testbibliothek.
- Testnamen auf Englisch und sprechend, z. B. `firstNoticeOnDayAfterDeadline`.
- Jeder Fehler bekommt zuerst einen Test, der ihn zeigt.

## Build

- Bauen und testen mit dem Maven Wrapper: `mvnw.cmd test` unter Windows, `./mvnw test` unter macOS und Linux.
- Keine neuen Abhängigkeiten ohne Rückfrage im Team.

## Code

- Bezeichner auf Englisch (`check`, `surcharge`).
- Java 21: Records für Datenklassen, `java.time` für Datumswerte, `Optional` statt `null` als Rückgabe.
