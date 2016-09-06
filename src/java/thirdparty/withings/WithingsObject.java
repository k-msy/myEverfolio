package thirdparty.withings;

public class WithingsObject {

    String dateStr = "";
    int steps = 0;
    long utcDate = 0L;
    Double weight = 0.0D;

    public String getDateStr() {
        return this.dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public int getSteps() {
        return this.steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public long getUtcDate() {
        return this.utcDate;
    }

    public void setUtcDate(long utcDate) {
        this.utcDate = utcDate;
    }

    public Double getWeight() {
        return this.weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
