package de.exercise.legacy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

/**
 * Liest die Zahlungsdatei der Bank (feste Satzlaenge, ein Satz je Zeile).
 *
 * <pre>
 * Pos  0- 9  Vertragsnummer, rechts mit Leerzeichen aufgefuellt
 * Pos 10-17  Buchungsdatum TTMMJJJJ
 * Pos 18-29  Betrag in Cent, 12 Stellen mit fuehrenden Nullen
 * Pos 30     Kennzeichen S (Soll) oder H (Haben)
 * Pos 31-57  Verwendungszweck
 * </pre>
 *
 * @author abr
 */
public class PaymentFileReader {

    public static final int RECORD_LENGTH = 58;

    public static int badLines = 0;

    private String charset = "ISO-8859-1";

    public PaymentFileReader() {
    }

    public PaymentFileReader(String charset) {
        this.charset = charset;
    }

    /**
     * @param pfad Pfad zur Zahlungsdatei
     * @return Vector mit Payment-Objekten oder null, wenn die Datei nicht existiert
     */
    public Vector readFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        badLines = 0;
        Vector payments = new Vector();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            String line = in.readLine();
            while (line != null) {
                if (line.length() != RECORD_LENGTH) {
                    badLines++;
                } else {
                    Payment z = readRecord(line);
                    if (z != null) {
                        payments.addElement(z);
                    } else {
                        badLines++;
                    }
                }
                line = in.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        fixSign(payments);
        return payments;
    }

    protected Payment readRecord(String line) {
        Payment z = new Payment();
        z.contractNo = line.substring(0, 10).trim();
        z.bookingDate = parseDate(line.substring(10, 18));
        if (z.bookingDate == null) {
            return null;
        }
        try {
            z.amountCents = Long.parseLong(line.substring(18, 30));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        z.code = line.substring(30, 31);
        z.purpose = line.substring(31, 58).trim();
        return z;
    }

    private Date parseDate(String text) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        sdf.setLenient(false);
        try {
            return sdf.parse(text);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void fixSign(Vector payments) {
        for (int i = 0; i < payments.size(); i++) {
            Payment z = (Payment) payments.elementAt(i);
            if ("H".equals(z.code)) {
                z.amountCents = -z.amountCents;
            }
        }
    }
}
