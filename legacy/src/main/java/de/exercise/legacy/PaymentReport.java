package de.exercise.legacy;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

/**
 * Summiert die Zahlungen einer Datei je Vertrag und gibt eine Liste aus.
 *
 * @author abr
 */
public class PaymentReport {

    public Hashtable sumPerContract(Vector payments) {
        Hashtable sums = new Hashtable();
        if (payments == null) {
            return sums;
        }
        for (int i = 0; i < payments.size(); i++) {
            Payment z = (Payment) payments.elementAt(i);
            Long previous = (Long) sums.get(z.contractNo);
            if (previous == null) {
                previous = new Long(0);
            }
            sums.put(z.contractNo, new Long(previous.longValue() + z.amountCents));
        }
        return sums;
    }

    public long grandTotal(Vector payments) {
        long sum = 0;
        if (payments != null) {
            for (int i = 0; i < payments.size(); i++) {
                sum += ((Payment) payments.elementAt(i)).amountCents;
            }
        }
        return sum;
    }

    public Vector creditNotes(Vector payments) {
        Vector result = new Vector();
        for (int i = 0; i < payments.size(); i++) {
            Payment z = (Payment) payments.elementAt(i);
            if (z.isCredit()) {
                result.addElement(z);
            }
        }
        return result;
    }

    public static String formatCents(long cents) {
        DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.GERMANY));
        return df.format(cents / 100.0);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Aufruf: PaymentReport <datei>");
            return;
        }
        PaymentFileReader reader = new PaymentFileReader();
        Vector payments = reader.readFile(args[0]);
        if (payments == null) {
            System.out.println("Datei nicht gefunden: " + args[0]);
            return;
        }
        PaymentReport paymentReport = new PaymentReport();
        Hashtable sums = paymentReport.sumPerContract(payments);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        for (int i = 0; i < payments.size(); i++) {
            Payment z = (Payment) payments.elementAt(i);
            System.out.println(z.contractNo + "  " + sdf.format(z.bookingDate) + "  "
                    + formatCents(z.amountCents) + "  " + z.purpose);
        }
        System.out.println("--");
        Enumeration e = sums.keys();
        while (e.hasMoreElements()) {
            String no = (String) e.nextElement();
            System.out.println(no + "  " + formatCents(((Long) sums.get(no)).longValue()));
        }
        System.out.println("Gesamt: " + formatCents(paymentReport.grandTotal(payments)));
        System.out.println("Fehlerhafte Zeilen: " + PaymentFileReader.badLines);
    }
}
