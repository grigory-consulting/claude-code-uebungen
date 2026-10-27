package de.exercise.legacy;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Eine Zahlung aus der Zahlungsdatei.
 *
 * @author abr
 */
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    public String contractNo;
    public Date bookingDate;
    public long amountCents;
    public String code;
    public String purpose;

    public Payment() {
    }

    public String getContractNo() {
        return contractNo;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(long amountCents) {
        this.amountCents = amountCents;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public boolean isCredit() {
        return "H".equals(code);
    }

    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String date = bookingDate != null ? sdf.format(bookingDate) : "?";
        return "Payment[" + contractNo + ", " + date + ", " + amountCents + ", " + code + ", "
                + purpose + "]";
    }
}
