package thirdparty.toggl;

public class TogglObject {

    String dateStr = "";
    long utcDate = 0L;
    long duration = 0L;

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

    public Long getDuration() {
        return this.duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}
