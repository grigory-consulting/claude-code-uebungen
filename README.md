# Vertragsabrechnung

Übungsprojekt: Abrechnung von Verträgen mit Tarifen, Rabatt, Steuer, Raten, Mahnungen und einem Leser für eine ältere Zahlungsdatei. Java 21, Maven, JUnit 5.

**Alle Daten in diesem Repository sind erfunden**, auch Namen, Adressen und IBANs unter `daten/`.

## Bauen und testen

Voraussetzung: JDK 21. Maven muss nicht installiert sein, der Maven Wrapper lädt es beim ersten Aufruf.

```
mvnw.cmd test        (Windows, Eingabeaufforderung oder PowerShell: .\mvnw.cmd test)
./mvnw test          (macOS, Linux, Git Bash)
```

## Module

- `hausfw`: Hausframework für Geldbeträge, Rundung, Logging, Konfiguration und Ablage.
- `abrechnung`: Verträge, Tarife, Monatsrechnung, Ratenplan, Mahnwesen; nutzt `hausfw`.
- `altbestand`: Leser für die Zahlungsdatei mit festen Feldbreiten, eigenständig.

Mehr in `docs/architektur.md` und `docs/teamstandards.md`.
