package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import thirdparty.todoist.Todoist;

@Named
@RequestScoped
public class UtilDate {

    LocalDateTime today;
    DateTimeFormatter formatter;

    public UtilDate() {
        this.today = LocalDateTime.now();
        this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    public String formatYyyyMmDd(Date date) {
        Instant instant = date.toInstant();
        LocalDateTime local = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
        return local.format(this.formatter);
    }

    public String getTodayYyyyMmDd() {
        return this.today.format(this.formatter);
    }

    public String getYesterDayYyyyMmDd() {
        LocalDateTime yesterday = this.today.minusDays(1L);
        return yesterday.format(this.formatter);
    }

    public String convertStartUTC(String from) {
        LocalDate date = LocalDate.parse(from, this.formatter);
        ZonedDateTime d = date.atStartOfDay(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
        System.out.println("start=" + String.valueOf(d.toEpochSecond()));
        return String.valueOf(d.toEpochSecond());
    }

    public String convertEndUTC(String to) {
        LocalDate date = LocalDate.parse(to, this.formatter);
        ZonedDateTime d = date.atStartOfDay(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
        System.out.println("end=" + String.valueOf(d.toEpochSecond() + 86399L));
        return String.valueOf(d.toEpochSecond() + 86399L);
    }

    public String convertUtcToYyyyMmDd(String utc) {
        Date date = new Date(Long.valueOf(utc) * 1000L);

        return formatYyyyMmDd(date);
    }

    public Date convertLocalDateTimeToDate(LocalDateTime local) {
        ZonedDateTime zDate = local.atZone(ZoneId.systemDefault());
        return Date.from(zDate.toInstant());
    }

    public ArrayList<String> getDayList(Date start, Date end) {
        ArrayList<String> dayList = new ArrayList();
        String toStr = formatYyyyMmDd(end);
        if (!start.equals(end)) {
            Instant instant = start.toInstant();
            LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
            do {
                dayList.add(date.format(this.formatter));
                date = date.plusDays(1L);
            } while (!toStr.equals(date.format(this.formatter)));
            dayList.add(toStr);
        } else {
            dayList.add(toStr);
        }
        return dayList;
    }

    public ArrayList<String> getYyyyMMddList(String start, String end) {
        ArrayList<String> dayList = new ArrayList();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dateStart = format.parse(start);
            Date dateEnd = format.parse(end);

            String toStr = formatYyyyMmDd(dateEnd);
            if (!start.equals(end)) {
                Instant instant = dateStart.toInstant();
                LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
                do {
                    dayList.add(date.format(this.formatter));
                    date = date.plusDays(1L);
                } while (!toStr.equals(date.format(this.formatter)));
                dayList.add(toStr);
            } else {
                dayList.add(toStr);
            }
            return dayList;
        } catch (ParseException ex) {
            Logger.getLogger(UtilDate.class.getName()).log(Level.SEVERE, null, ex);
        }
        return dayList;
    }

    public String convertUSformatToYyyy_mm_dd(String due_date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

        String formatted = "";
        try {
            Date date = sdf.parse(due_date);
            formatted = formatYyyyMmDd(date);
        } catch (ParseException ex) {
            Logger.getLogger(Todoist.class.getName()).log(Level.SEVERE, null, ex);
        }
        return formatted;
    }

    public boolean isToday(Date start, Date end) {
        String todayStr = getTodayYyyyMmDd();
        String startStr = formatYyyyMmDd(start);
        String endStr = formatYyyyMmDd(end);
        if ((todayStr.equals(startStr)) && (todayStr.equals(endStr))) {
            return true;
        }
        return false;
    }

    public ArrayList<String> summarizeDayList(ArrayList<String> dayList) {
        ArrayList<String> summarizedDayList = new ArrayList();
        for (int i = 0; i < dayList.size(); i++) {
            String start = (String) dayList.get(i);
            int sum = 0;
            for (int j = 0; j < 7; j++) {
                if (i > dayList.size()) {
                    break;
                }
                if (j == 6) {
                    i += j;
                }
            }
            String end;
            if (i <= dayList.size()) {
                end = (String) dayList.get(i);
            } else {
                end = (String) dayList.get(dayList.size() - 1);
            }
            summarizedDayList.add(start + "〜" + end);
        }
        return summarizedDayList;
    }
}
