/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oauth;

import ch.simas.jtoggl.Client;
import ch.simas.jtoggl.JToggl;
import ch.simas.jtoggl.Project;
import ch.simas.jtoggl.Task;
import ch.simas.jtoggl.TimeEntry;
import ch.simas.jtoggl.Workspace;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.SessionScoped;

/**
 *
 * @author bpg0129
 */
@SessionScoped
public class Toggl extends SuperOauth {

    private static JToggl jToggl;
    private static TimeEntry timeEntry;
    private static Client client;
    private static Project project;
    private static Task task;
    private static Workspace workspace;

    private static final String api_token = "101872f22ff214f3902d2e9ae565264c";
    private static final String togglMe = "https://www.toggl.com/api/v8/me";
    //private static final String method = "GET";
    
    

    public Toggl() {

    }

    public Map<String, Map<String, String>> getTodayDuration() throws IOException {
        /*
         String togglApiToken = System.getenv("TOGGL_API_TOKEN");
         if (togglApiToken == null) {
         togglApiToken = System.getProperty("TOGGL_API_TOKEN");
         if (togglApiToken == null) {
         throw new RuntimeException("TOGGL_API_TOKEN not set.");
         }
         }
         */
        jToggl = new JToggl(api_token, "api_token");
        jToggl.setThrottlePeriod(500l);
        jToggl.switchLoggingOn();
        List<Workspace> workspaces = jToggl.getWorkspaces();
        workspace = workspaces.get(0);

        //LocalDate fromDate = LocalDate.now().minusDays(1);
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = LocalDate.now().plusDays(1);

        List<TimeEntry> timeEntryList = jToggl.getTimeEntries(Date.valueOf(fromDate), Date.valueOf(toDate));
        
        //pidごとのtimeEntryにまとめる
        Map<String, Map<String, String>> timeEntryMap = new HashMap<>();
        Map<String, String> projectMap = null;
        long durations = 0;
        for (TimeEntry timeEntry : timeEntryList) {
            String pid = String.valueOf(timeEntry.getPid());
            if (timeEntryMap.size() == 0) {
                projectMap = new HashMap<>();
                projectMap.put("pid", pid);
                projectMap.put("description", timeEntry.getDescription());
                projectMap.put("duration", String.valueOf(timeEntry.getDuration()));
                timeEntryMap.put(pid, projectMap);
            } else if (timeEntryMap.get(pid).containsKey("pid")) {
                Long duration = Long.valueOf(timeEntryMap.get(pid).get("duration"));
                if (timeEntry.getDuration() > 0) {
                    duration = duration + timeEntry.getDuration();
                    if (projectMap != null) {
                        projectMap.put("duration", String.valueOf(duration));
                        timeEntryMap.put(pid, projectMap);
                    } else {
                        System.out.println("projectMapはNULL!!");
                    }
                }

            } else {
                projectMap = new HashMap<>();
                projectMap.put("pid", pid);
                projectMap.put("description", timeEntry.getDescription());
                projectMap.put("duration", String.valueOf(timeEntry.getDuration()));
                timeEntryMap.put(pid, projectMap);
            }
        }
        long totalDurations = 0;
        for (Map.Entry<String, Map<String, String>> entry : timeEntryMap.entrySet()) {
            totalDurations = totalDurations + Long.valueOf(entry.getValue().get("duration"));
        }
        Map<String, String> totalDurationMap = new HashMap<>();
        totalDurationMap.put("totalDurations", String.valueOf(totalDurations));
        timeEntryMap.put("totalDurations", totalDurationMap);

        return timeEntryMap;
    }

    public String convertHms(long durations) {
        long hours = 0;
        long minutes = 0;
        long seconds = 0;
        if (0 < durations) {
            if (3600 < durations) {
                //1時間以上
                hours = (long) Math.floor(durations / 3600);
                minutes = (long) Math.floor(durations - (3600 * hours)) / 60;
                seconds = (long) Math.floor(durations - (3600 * hours)) - (minutes * 60);
            } else if (60 < durations) {
                //1分以上
                minutes = (long) Math.floor(durations / 60);
                seconds = (long) Math.floor(durations - (3600 * hours)) - (minutes * 60);
            } else {
                //それ以外
                seconds = durations;
            }
        }
        /*
         System.out.println("Hours = " + String.valueOf(hours));
         System.out.println("Minutes = " + String.valueOf(minutes));
         System.out.println("Seconds = " + String.valueOf(seconds));
         */
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
