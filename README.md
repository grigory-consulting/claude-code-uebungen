# Vertragsabrechnung

Übungsprojekt: Abrechnung von Verträgen mit Tarifen, Rabatt, Steuer, Raten, Mahnungen und einem Leser für eine ältere Zahlungsdatei. Java 21, Maven, JUnit 5.

**Alle Daten in diesem Repository sind erfunden**, auch Namen, Adressen und IBANs unter `data/`.

## Bauen und testen

Voraussetzung: JDK 21. Maven muss nicht installiert sein, der Maven Wrapper lädt es beim ersten Aufruf.

```
mvnw.cmd test        (Windows, Eingabeaufforderung oder PowerShell: .\mvnw.cmd test)
./mvnw test          (macOS, Linux, Git Bash)
```

## Module

- `core`: Hausframework für Geldbeträge, Rundung, Logging, Konfiguration und Ablage.
- `billing`: Verträge, Tarife, Monatsrechnung, Ratenplan, Mahnwesen; nutzt `core`.
- `legacy`: Leser für die Zahlungsdatei mit festen Feldbreiten, eigenständig.

Mehr in `docs/architecture.md` und `docs/team-standards.md`.
