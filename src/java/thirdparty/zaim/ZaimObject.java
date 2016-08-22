package thirdparty.zaim;

public class ZaimObject {

    String dateStr = "";
    long utcDate = 0L;
    int payment = 0;
    int income = 0;

    public String getDateStr() {
        return this.dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public long getUtcDate() {
        return this.utcDate;
    }

    public void setUtcDate(long utcDate) {
        this.utcDate = utcDate;
    }

    public int getPayment() {
        return this.payment;
    }

    public void setPayment(int payment) {
        this.payment = payment;
    }

    public int getIncome() {
        return this.income;
    }

    public void setIncome(int income) {
        this.income = income;
    }
}
