package thirdparty.todoist;

public class TodoistObject {

    String dateStr = "";
    long utcDate = 0L;
    long karma = 0L;

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

    public long getKarma() {
        return this.karma;
    }

    public void setKarma(long karma) {
        this.karma = karma;
    }
}
