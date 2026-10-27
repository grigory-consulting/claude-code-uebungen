package de.uebung.abrechnung;

import de.uebung.hausfw.Ablage;
import de.uebung.hausfw.Betrag;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Zahlungseingänge je Rechnung und der daraus offene Betrag.
 */
public class OffenePosten {

    private final Ablage<Rechnung> rechnungen;
    private final Map<String, Betrag> gezahlt = new HashMap<>();

    public OffenePosten(Ablage<Rechnung> rechnungen) {
        this.rechnungen = rechnungen;
    }

    public void buche(String rechnungsId, Betrag zahlung) {
        if (rechnungen.lade(rechnungsId).isEmpty()) {
            throw new NoSuchElementException("Unbekannte Rechnung: " + rechnungsId);
        }
        if (zahlung.istNegativ() || zahlung.equals(Betrag.NULL)) {
            throw new IllegalArgumentException("Zahlung muss positiv sein: " + zahlung);
        }
        gezahlt.merge(rechnungsId, zahlung, Betrag::plus);
    }

    public Betrag gezahlt(Rechnung rechnung) {
        return gezahlt.getOrDefault(rechnung.id(), Betrag.NULL);
    }

    public Betrag offen(Rechnung rechnung) {
        return rechnung.brutto().minus(gezahlt(rechnung));
    }

    public boolean istBezahlt(Rechnung rechnung) {
        return offen(rechnung).compareTo(Betrag.NULL) <= 0;
    }

    public List<Rechnung> ueberfaellig(LocalDate stichtag) {
        return rechnungen.alle().stream()
                .filter(r -> r.faellig().isBefore(stichtag))
                .filter(r -> !istBezahlt(r))
                .sorted(Comparator.comparing(Rechnung::faellig).thenComparing(Rechnung::id))
                .toList();
    }

    public Betrag summeOffen() {
        Betrag summe = Betrag.NULL;
        for (Rechnung rechnung : rechnungen.alle()) {
            if (!istBezahlt(rechnung)) {
                summe = summe.plus(offen(rechnung));
            }
        }
        return summe;
    }
}
