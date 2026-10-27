# Architektur

Drei Maven-Module unter dem Root-Projekt `vertragsabrechnung`:

```
abrechnung  ──>  hausfw
altbestand       (steht allein)
```

## hausfw

Das Hausframework. Es kapselt alles, was Fachcode nicht selbst lösen soll: Geldbeträge (`Betrag`, intern in Cent) und ihre Rundung (`Rundung`), Logging (`HausLog`, schreibt nach stderr), Einstellungen (`Konfiguration`, liest `abrechnung.properties` aus dem Klassenpfad) und Persistenz (`Ablage<T>`, im Test und in der Entwicklung `SpeicherAblage<T>`). Das Modul hängt von nichts ab.

## abrechnung

Die Vertragsabrechnung. Verträge (`Vertrag`) gehören zu einem Mandanten und haben einen Tarif mit Monatsbeitrag und Rabatt. `RechnungsRechner` erstellt die Monatsrechnung, `Mahnwesen` prüft offene Rechnungen gegen die Mahnfrist, `Ratenplan` teilt einen Betrag in Raten. `VertragsService` verbindet Ablagen, Rechner und Mahnwesen und ist der Einstieg für Aufrufer. Die Einstellungen liegen in `abrechnung/src/main/resources/abrechnung.properties`.

## altbestand

Einlesen der Zahlungsdatei der Bank (feste Satzlänge, Format im Javadoc von `ZahlungsdateiLeser`) und eine einfache Auswertung je Vertrag. Der Code stammt aus einem älteren System und nutzt weder das Hausframework noch `java.time`. Beispieldatei: `daten/beispiel/zahlungen-2026-09.txt`.
