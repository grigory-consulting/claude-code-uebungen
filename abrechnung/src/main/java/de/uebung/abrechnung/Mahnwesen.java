package de.uebung.abrechnung;

import de.uebung.hausfw.Betrag;
import de.uebung.hausfw.Konfiguration;
import de.uebung.hausfw.Rundung;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

public class Mahnwesen {

    static final int HOECHSTE_STUFE = 3;

    private final Betrag mahngebuehr;
    private final int mahnfristTage;

    public Mahnwesen(Konfiguration konfiguration) {
        this.mahngebuehr = konfiguration.mahngebuehr();
        this.mahnfristTage = konfiguration.mahnfristTage();
    }

    public Optional<Mahnung> pruefe(Rechnung rechnung, LocalDate heute, boolean bezahlt) {
        Objects.requireNonNull(rechnung, "rechnung");
        Objects.requireNonNull(heute, "heute");
        if (bezahlt) {
            return Optional.empty();
        }
        LocalDate fristende = rechnung.faellig().plusDays(mahnfristTage);
        if (!heute.isAfter(fristende)) {
            return Optional.empty();
        }
        int stufe = stufe(fristende, heute);
        Betrag gebuehr = mahngebuehr.mal(BigDecimal.valueOf(stufe), Rundung.KAUFMAENNISCH);
        return Optional.of(new Mahnung(rechnung, stufe, gebuehr, heute));
    }

    private int stufe(LocalDate fristende, LocalDate heute) {
        long tageUeberFrist = ChronoUnit.DAYS.between(fristende, heute);
        long stufe = 1 + (tageUeberFrist - 1) / mahnfristTage;
        return (int) Math.min(stufe, HOECHSTE_STUFE);
    }
}
