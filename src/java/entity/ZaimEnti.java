package entity;

import java.io.Serializable;
import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("zaEnti")
@RequestScoped
public class ZaimEnti implements Serializable {

    private ArrayList<String> paymentList;
    private ArrayList<String> incomeList;
    private String sumPayment;
    private String sumIncome;

    public ArrayList<String> getPaymentList() {
        return this.paymentList;
    }

    public void setPaymentList(ArrayList<String> paymentList) {
        this.paymentList = paymentList;
    }

    public ArrayList<String> getIncomeList() {
        return this.incomeList;
    }

    public void setIncomeList(ArrayList<String> incomeList) {
        this.incomeList = incomeList;
    }

    public String getSumPayment() {
        return this.sumPayment;
    }

    public void setSumPayment(String sumPayment) {
        this.sumPayment = sumPayment;
    }

    public String getSumIncome() {
        return this.sumIncome;
    }

    public void setSumIncome(String sumIncome) {
        this.sumIncome = sumIncome;
    }
}
