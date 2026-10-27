# Architektur

Drei Maven-Module unter dem Root-Projekt `contract-billing`:

```
billing  ──>  core
legacy       (steht allein)
```

## core

Das Hausframework. Es kapselt alles, was Fachcode nicht selbst lösen soll: Geldbeträge (`Money`, intern in Cent) und ihre Rundung (`Rounding`), Logging (`CoreLog`, schreibt nach stderr), Einstellungen (`Configuration`, liest `billing.properties` aus dem Klassenpfad) und Persistenz (`Storage<T>`, im Test und in der Entwicklung `InMemoryStorage<T>`). Das Modul hängt von nichts ab.

## billing

Die Vertragsabrechnung. Verträge (`Contract`) gehören zu einem Mandanten und haben einen Tarif mit Monatsbeitrag und Rabatt. `InvoiceCalculator` erstellt die Monatsrechnung, `PaymentReminder` prüft offene Rechnungen gegen die Mahnfrist, `InstallmentPlan` teilt einen Betrag in Raten. `ContractService` verbindet Ablagen, Rechner und Mahnwesen und ist der Einstieg für Aufrufer. Die Einstellungen liegen in `billing/src/main/resources/billing.properties`.

## legacy

Einlesen der Zahlungsdatei der Bank (feste Satzlänge, Format im Javadoc von `PaymentFileReader`) und eine einfache Auswertung je Vertrag. Der Code stammt aus einem älteren System und nutzt weder das Hausframework noch `java.time`. Beispieldatei: `data/sample/payments-2026-09.txt`.
