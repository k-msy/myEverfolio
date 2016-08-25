package thirdparty.toggl;

import ch.simas.jtoggl.*;
import java.io.IOException;
import java.time.*;
import java.util.*;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import oauth.SuperOauth;
import util.UtilDate;
import util.UtilLogic;
import view.chart.BarChart;

@RequestScoped
public class Toggl extends SuperOauth {

    private static JToggl jToggl;
    private static TimeEntry timeEntry;
    private static Client client;
    private static Project project;
    private static Task task;
    private static Workspace workspace;
    @Inject
    UtilDate utiDate;
    @Inject
    BarChart barChart;
    @Inject
    UtilLogic utiLogic;

    public Toggl() {
        jToggl = new JToggl("101872f22ff214f3902d2e9ae565264c", "api_token");
        jToggl.setThrottlePeriod(500L);
        jToggl.switchLoggingOn();
        List<Workspace> workspaces = jToggl.getWorkspaces();
        workspace = (Workspace) workspaces.get(0);
    }

    public ArrayList<String[]> getTodayDuration() throws IOException {

        LocalDate today = LocalDate.now();
        Date from = this.utiDate.convertLocalDateTimeToDate(today.atTime(0, 0, 0));
        Date to = this.utiDate.convertLocalDateTimeToDate(today.atTime(23, 59, 59));

        List<TimeEntry> timeEntryList = jToggl.getTimeEntries(from, to);

        String[] project = new String[3];
        ArrayList<String[]> projectList = new ArrayList();
        for (TimeEntry timeEntry : timeEntryList) {
            String pid = String.valueOf(timeEntry.getPid());
            if (projectList.isEmpty()) {
                if (0L < timeEntry.getDuration()) {
                    project[0] = pid;
                    project[1] = timeEntry.getDescription();
                    project[2] = String.valueOf(timeEntry.getDuration());
                    projectList.add(project);
                }
            } else {
                int samePidIndex = this.utiLogic.getSameValueIndex(projectList, pid);
                if (0 <= samePidIndex) {
                    if (0L < timeEntry.getDuration()) {
                        String[] tmpProject = (String[]) projectList.get(samePidIndex);
                        tmpProject[2] = String.valueOf(Long.valueOf(tmpProject[2]) + timeEntry.getDuration());
                        projectList.set(samePidIndex, tmpProject);
                    }
                } else if (0L < timeEntry.getDuration()) {
                    project[0] = pid;
                    project[1] = timeEntry.getDescription();
                    project[2] = String.valueOf(timeEntry.getDuration());
                    projectList.add(project);
                }
            }
        }
        return projectList;
    }

    public void setRangeMeasures(Date start, Date end, ArrayList<String> dayList, int dayCount) {
        LocalDate lFrom = ZonedDateTime.ofInstant(start.toInstant(), ZoneId.systemDefault()).toLocalDate();
        LocalDate lTo = ZonedDateTime.ofInstant(end.toInstant(), ZoneId.systemDefault()).toLocalDate();
        Date from = this.utiDate.convertLocalDateTimeToDate(lFrom.atTime(0, 0, 0));
        Date to = this.utiDate.convertLocalDateTimeToDate(lTo.atTime(23, 59, 59));

        List<TimeEntry> timeEntryList = jToggl.getTimeEntries(from, to);
        ArrayList<TogglObject> dayDurationsList = getDayDurationsList(timeEntryList);

        injectZeroDayData(dayList, dayDurationsList);

        Collections.sort(dayDurationsList, new TogglComparator());
        if (dayDurationsList.size() > 60) {
            dayDurationsList = summarizeMonthDuration(dayDurationsList);
        } else if (dayDurationsList.size() > 31) {
            dayDurationsList = summarizeWeekDuration(dayDurationsList);
        }
        this.barChart.setBarModelToggl(dayDurationsList);
    }

    private int getSameDayIndex(ArrayList<TogglObject> dayDurationsList, String dateStr) {
        for (int index = 0; index < dayDurationsList.size(); index++) {
            if (dateStr.equals(((TogglObject) dayDurationsList.get(index)).dateStr)) {
                return index;
            }
        }
        return -1;
    }

    public long getTotalDurations(ArrayList<String[]> projectList) {
        long totalDurations = 0L;
        for (String[] project : projectList) {
            totalDurations += Long.valueOf(project[2]);
        }
        return totalDurations;
    }

    public String convertHms(long durations) {
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        if (0 < durations) {
            if (3600 < durations) {
                hours = (long) Math.floor(durations / 3600);
                minutes = (long) (Math.floor(durations - 3600 * hours) / 60);
                seconds = (long) (Math.floor(durations - 3600 * hours) - minutes * 60);
            } else if (60 < durations) {
                minutes = (long) Math.floor(durations / 60);
                seconds = (long) (Math.floor(durations - 3600L * hours) - minutes * 60);
            } else {
                seconds = durations;
            }
        }
        return String.format("%02d:%02d:%02d", new Object[]{hours, minutes, seconds});
    }

    public Number formatHhMm(Long durations) {
        long hours = 0;
        long minutes = 0;
        if (0 < durations) {
            if (3600 < durations) {
                hours = (long) Math.floor(durations / 3600);
                minutes = (long) (Math.floor(durations - 3600 * hours) / 60);
            } else if (60 < durations) {
                minutes = (long) Math.floor(60 / durations);
            }
        }
        String tmp = String.valueOf(hours) + String.valueOf(minutes);

        return Integer.valueOf(tmp);
    }

    public ArrayList<String> convertProjectList(ArrayList<String[]> tmpProjectList) {
        ArrayList<String> list = new ArrayList();
        for (String[] tmp : tmpProjectList) {
            list.add(tmp[1] + ":  " + convertHms(Long.valueOf(tmp[2])));
        }
        return list;
    }

    private ArrayList<TogglObject> getDayDurationsList(List<TimeEntry> timeEntryList) {
        ArrayList<TogglObject> dayDurationsList = new ArrayList();
        for (TimeEntry timeEntry : timeEntryList) {
            TogglObject togglObj = new TogglObject();

            Date date = timeEntry.getStart();
            String yyyy_MM_dd_Str = this.utiDate.formatYyyyMmDd(date);
            String mm_DD_Str = yyyy_MM_dd_Str.substring(5);
            Long duration = timeEntry.getDuration();
            if (0L < duration) {
                if (dayDurationsList.isEmpty()) {
                    togglObj.dateStr = mm_DD_Str;
                    togglObj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(yyyy_MM_dd_Str));
                    togglObj.duration = duration;
                    dayDurationsList.add(togglObj);
                } else {
                    int index = getSameDayIndex(dayDurationsList, mm_DD_Str);
                    if (0 <= index) {
                        TogglObject tmpObj = (TogglObject) dayDurationsList.get(index);
                        tmpObj.duration += duration;
                        dayDurationsList.set(index, tmpObj);
                    } else {
                        togglObj.dateStr = mm_DD_Str;
                        togglObj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(yyyy_MM_dd_Str));
                        togglObj.duration = duration;
                        dayDurationsList.add(togglObj);
                    }
                }
            }
        }
        return dayDurationsList;
    }

    private void injectZeroDayData(ArrayList<String> dayList, ArrayList<TogglObject> dayDurationsList) {
        Iterator localIterator;
        String day;
        if (dayDurationsList.isEmpty()) {
            for (localIterator = dayList.iterator(); localIterator.hasNext();) {
                day = (String) localIterator.next();
                TogglObject obj = new TogglObject();
                obj.dateStr = day;
                obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC(day));
                obj.duration = 0L;
                dayDurationsList.add(obj);
            }
        } else {
            Object list = new ArrayList();
            for (TogglObject obj : dayDurationsList) {
                ((ArrayList) list).add(obj.dateStr);
            }
            for (int i = 0; i < dayList.size(); i++) {
                String date = ((String) dayList.get(i)).substring(5);
                int index = ((ArrayList) list).indexOf(date);
                if (index < 0) {
                    TogglObject obj = new TogglObject();
                    obj.dateStr = ((String) dayList.get(i)).substring(5);
                    obj.utcDate = Long.valueOf(this.utiDate.convertStartUTC((String) dayList.get(i)));
                    obj.duration = 0L;
                    dayDurationsList.add(obj);
                }
            }
        }
    }

    private ArrayList<TogglObject> summarizeMonthDuration(ArrayList<TogglObject> dayDurationsList) {
        ArrayList<TogglObject> sumDurationsList = new ArrayList();
        for (TogglObject obj : dayDurationsList) {
            if (!sumDurationsList.isEmpty()) {
                boolean sumFlg = false;
                for (int i = 0; i < sumDurationsList.size(); i++) {
                    String month = obj.dateStr.substring(0, 2);
                    if (month.equals(((TogglObject) sumDurationsList.get(i)).dateStr)) {
                        long sum = ((TogglObject) sumDurationsList.get(i)).duration + obj.duration;
                        ((TogglObject) sumDurationsList.get(i)).duration = sum;
                        sumFlg = true;
                    }
                }
                if (!sumFlg) {
                    obj.dateStr = obj.dateStr.substring(0, 2);
                    sumDurationsList.add(obj);
                }
            } else {
                obj.dateStr = obj.dateStr.substring(0, 2);
                sumDurationsList.add(obj);
            }
        }
        return sumDurationsList;
    }

    private ArrayList<TogglObject> summarizeWeekDuration(ArrayList<TogglObject> dayDurationsList) {
        ArrayList<TogglObject> sumDurationsList = new ArrayList();
        for (int i = 0; i < dayDurationsList.size(); i++) {
            String start = ((TogglObject) dayDurationsList.get(i)).dateStr;
            long utcDate = ((TogglObject) dayDurationsList.get(i)).utcDate;
            long sum = 0L;
            for (int j = 0; j < 7; j++) {
                if (i >= dayDurationsList.size()) {
                    break;
                }
                sum += ((TogglObject) dayDurationsList.get(i)).duration;
                i += 1;
            }
            String end = findEndDate(i - 1, dayDurationsList);
            TogglObject obj = new TogglObject();
            obj.dateStr = (start + "〜" + end);
            obj.utcDate = utcDate;
            obj.duration = sum;
            sumDurationsList.add(obj);
        }
        return sumDurationsList;
    }

    private String findEndDate(int i, ArrayList<TogglObject> list) {
        String end;
        if (i < list.size()) {
            end = ((TogglObject) list.get(i)).dateStr;
        } else {
            end = ((TogglObject) list.get(list.size() - 1)).dateStr;
        }
        return end;
    }
}
