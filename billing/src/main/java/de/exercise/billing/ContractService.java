package de.exercise.billing;

import de.exercise.core.Storage;
import de.exercise.core.Money;
import de.exercise.core.CoreLog;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ContractService {

    private static final CoreLog LOG = CoreLog.forClass(ContractService.class);

    private final Storage<Contract> contracts;
    private final Storage<Invoice> invoices;
    private final InvoiceCalculator calculator;
    private final PaymentReminder paymentReminder;
    private final InstallmentPlan installmentPlan = new InstallmentPlan();

    public ContractService(Storage<Contract> contracts, Storage<Invoice> invoices,
                           InvoiceCalculator calculator, PaymentReminder paymentReminder) {
        this.contracts = contracts;
        this.invoices = invoices;
        this.calculator = calculator;
        this.paymentReminder = paymentReminder;
    }

    public void create(Contract contract) {
        if (contracts.load(contract.number()).isPresent()) {
            throw new IllegalStateException("Vertrag existiert bereits: " + contract.number());
        }
        contracts.save(contract.number(), contract);
        LOG.info("Vertrag {} angelegt, Tarif {}, Beginn {}",
                contract.number(), contract.tariff().name(), contract.start());
    }

    public Contract contract(String number) {
        return contracts.load(number)
                .orElseThrow(() -> new NoSuchElementException("Unbekannter Vertrag: " + number));
    }

    public void changeTariff(String number, Tariff newTariff) {
        Contract previous = contract(number);
        contracts.save(number, previous.withTariff(newTariff));
        LOG.info("Vertrag {}: Tarif {} -> {}", number, previous.tariff().name(), newTariff.name());
    }

    public Invoice bill(String number, YearMonth month) {
        Contract contract = contract(number);
        Optional<Invoice> existing = invoices.load(number + "/" + month);
        if (existing.isPresent()) {
            LOG.warn("Rechnung {} für {} existiert bereits", month, number);
            return existing.get();
        }
        Invoice invoice = calculator.calculate(contract, month);
        invoices.save(invoice.id(), invoice);
        LOG.info("Rechnung {} für {} erstellt: {}", month, number, invoice.gross());
        return invoice;
    }

    public List<Invoice> monthlyRun(YearMonth month) {
        List<Invoice> created = new ArrayList<>();
        for (Contract contract : contracts.all()) {
            if (contract.runsIn(month)) {
                created.add(bill(contract.number(), month));
            }
        }
        LOG.info("Monatslauf {}: {} Rechnungen, Summe {}", month, created.size(), sum(created));
        return created;
    }

    public List<Invoice> invoicesFor(String number) {
        return invoices.all().stream()
                .filter(r -> r.contract().number().equals(number))
                .toList();
    }

    public Optional<ReminderNotice> checkReminder(String number, YearMonth month, LocalDate today, boolean paid) {
        Invoice invoice = invoices.load(number + "/" + month)
                .orElseThrow(() -> new NoSuchElementException("Keine Rechnung " + month + " für " + number));
        Optional<ReminderNotice> notice = paymentReminder.check(invoice, today, paid);
        notice.ifPresent(m -> LOG.info("Mahnung Stufe {} für {} ({}), Gebühr {}",
                m.level(), number, month, m.fee()));
        return notice;
    }

    public List<ReminderNotice> reminderRun(OpenItems items, LocalDate today) {
        List<ReminderNotice> notices = new ArrayList<>();
        for (Invoice invoice : items.overdue(today)) {
            paymentReminder.check(invoice, today, false).ifPresent(notices::add);
        }
        LOG.info("Mahnlauf {}: {} Mahnungen, offen gesamt {}", today, notices.size(), items.sumOpen());
        return notices;
    }

    public List<Money> installmentPlan(String number, YearMonth month, int count) {
        Invoice invoice = bill(number, month);
        List<Money> installments = installmentPlan.installments(invoice.gross(), count);
        LOG.info("Ratenplan für {} ({}): {} Raten zu {}", number, month, count, installments.get(0));
        return installments;
    }

    static Money sum(List<Invoice> list) {
        Money sum = Money.ZERO;
        for (Invoice invoice : list) {
            sum = sum.plus(invoice.gross());
        }
        return sum;
    }
}
