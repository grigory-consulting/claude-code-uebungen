package de.uebung.abrechnung;

import de.uebung.hausfw.Ablage;
import de.uebung.hausfw.Betrag;
import de.uebung.hausfw.HausLog;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class VertragsService {

    private static final HausLog LOG = HausLog.fuer(VertragsService.class);

    private final Ablage<Vertrag> vertraege;
    private final Ablage<Rechnung> rechnungen;
    private final RechnungsRechner rechner;
    private final Mahnwesen mahnwesen;
    private final Ratenplan ratenplan = new Ratenplan();

    public VertragsService(Ablage<Vertrag> vertraege, Ablage<Rechnung> rechnungen,
                           RechnungsRechner rechner, Mahnwesen mahnwesen) {
        this.vertraege = vertraege;
        this.rechnungen = rechnungen;
        this.rechner = rechner;
        this.mahnwesen = mahnwesen;
    }

    public void anlegen(Vertrag vertrag) {
        if (vertraege.lade(vertrag.nummer()).isPresent()) {
            throw new IllegalStateException("Vertrag existiert bereits: " + vertrag.nummer());
        }
        vertraege.speichere(vertrag.nummer(), vertrag);
        LOG.info("Vertrag {} angelegt, Tarif {}, Beginn {}",
                vertrag.nummer(), vertrag.tarif().name(), vertrag.beginn());
    }

    public Vertrag vertrag(String nummer) {
        return vertraege.lade(nummer)
                .orElseThrow(() -> new NoSuchElementException("Unbekannter Vertrag: " + nummer));
    }

    public void wechsleTarif(String nummer, Tarif neuerTarif) {
        Vertrag bisher = vertrag(nummer);
        vertraege.speichere(nummer, bisher.mitTarif(neuerTarif));
        LOG.info("Vertrag {}: Tarif {} -> {}", nummer, bisher.tarif().name(), neuerTarif.name());
    }

    public Rechnung rechneAb(String nummer, YearMonth monat) {
        Vertrag vertrag = vertrag(nummer);
        Optional<Rechnung> vorhanden = rechnungen.lade(nummer + "/" + monat);
        if (vorhanden.isPresent()) {
            LOG.warnung("Rechnung {} für {} existiert bereits", monat, nummer);
            return vorhanden.get();
        }
        Rechnung rechnung = rechner.berechne(vertrag, monat);
        rechnungen.speichere(rechnung.id(), rechnung);
        LOG.info("Rechnung {} für {} erstellt: {}", monat, nummer, rechnung.brutto());
        return rechnung;
    }

    public List<Rechnung> monatslauf(YearMonth monat) {
        List<Rechnung> erstellt = new ArrayList<>();
        for (Vertrag vertrag : vertraege.alle()) {
            if (vertrag.laeuftIm(monat)) {
                erstellt.add(rechneAb(vertrag.nummer(), monat));
            }
        }
        LOG.info("Monatslauf {}: {} Rechnungen, Summe {}", monat, erstellt.size(), summe(erstellt));
        return erstellt;
    }

    public List<Rechnung> rechnungenZu(String nummer) {
        return rechnungen.alle().stream()
                .filter(r -> r.vertrag().nummer().equals(nummer))
                .toList();
    }

    public Optional<Mahnung> pruefeMahnung(String nummer, YearMonth monat, LocalDate heute, boolean bezahlt) {
        Rechnung rechnung = rechnungen.lade(nummer + "/" + monat)
                .orElseThrow(() -> new NoSuchElementException("Keine Rechnung " + monat + " für " + nummer));
        Optional<Mahnung> mahnung = mahnwesen.pruefe(rechnung, heute, bezahlt);
        mahnung.ifPresent(m -> LOG.info("Mahnung Stufe {} für {} ({}), Gebühr {}",
                m.stufe(), nummer, monat, m.gebuehr()));
        return mahnung;
    }

    public List<Mahnung> mahnlauf(OffenePosten posten, LocalDate heute) {
        List<Mahnung> mahnungen = new ArrayList<>();
        for (Rechnung rechnung : posten.ueberfaellig(heute)) {
            mahnwesen.pruefe(rechnung, heute, false).ifPresent(mahnungen::add);
        }
        LOG.info("Mahnlauf {}: {} Mahnungen, offen gesamt {}", heute, mahnungen.size(), posten.summeOffen());
        return mahnungen;
    }

    public List<Betrag> ratenplan(String nummer, YearMonth monat, int anzahl) {
        Rechnung rechnung = rechneAb(nummer, monat);
        List<Betrag> raten = ratenplan.raten(rechnung.brutto(), anzahl);
        LOG.info("Ratenplan für {} ({}): {} Raten zu {}", nummer, monat, anzahl, raten.get(0));
        return raten;
    }

    static Betrag summe(List<Rechnung> liste) {
        Betrag summe = Betrag.NULL;
        for (Rechnung rechnung : liste) {
            summe = summe.plus(rechnung.brutto());
        }
        return summe;
    }
}
