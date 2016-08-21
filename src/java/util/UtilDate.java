/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class UtilDate {

    /**
     * 今日の日付を取得する
     *
     * @param date
     * @param formatted
     * @return
     */
    public String formatYyyyMmDd(Date date, SimpleDateFormat formatted) {
        return formatted.format(date);
    }

    /**
     * 今日の日付から、昨日の日付を取得する
     *
     * @param date
     * @param formatted
     * @return
     */
    public String getYesterDayYyyyMmDd(Date date, SimpleDateFormat formatted) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        return formatted.format(yesterday.getTime());
    }

    /**
     * 開始日（00:00:00）をUNIX時に変換
     *
     * @param from
     * @return
     */
    public String convertStartUTC(String from) {
        ZonedDateTime d;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(from, formatter);
        d = date.atStartOfDay(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
        System.out.println("start=" + String.valueOf(d.toEpochSecond()));
        return String.valueOf(d.toEpochSecond());
    }

    /**
     * 終了日（23:59:59）をUNIX時に変換
     *
     * @param to
     * @return
     */
    public String convertEndUTC(String to) {
        ZonedDateTime d;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(to, formatter);
        d = date.atStartOfDay(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
        System.out.println("end=" + String.valueOf(d.toEpochSecond() + 86399));
        return String.valueOf(d.toEpochSecond() + 86399);
    }

    public String convertUtcToYyyyMmDd(String utc) { 
        Date date = new Date(Long.valueOf(utc) * 1000);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
     
        return formatYyyyMmDd(date, formatter);
    }


}
