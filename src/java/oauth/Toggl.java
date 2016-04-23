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
import java.util.ArrayList;
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

    public ArrayList<String[]> getTodayDuration() throws IOException {
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
        String[] project = new String[3];
        ArrayList<String[]> projectList = new ArrayList<>();

        for (TimeEntry timeEntry : timeEntryList) {
            String pid = String.valueOf(timeEntry.getPid());
            if (projectList.isEmpty()) {
                if (0 < timeEntry.getDuration()) {
                    project[0] = pid;
                    project[1] = timeEntry.getDescription();
                    project[2] = String.valueOf(timeEntry.getDuration());
                    projectList.add(project);
                }
            } else {
                int samePidIndex;
                samePidIndex = getSamePidIndex(projectList, pid);
                if (0 <= samePidIndex) {
                    //同じpidを持つprojectが存在した時
                    if (0 < timeEntry.getDuration()) {
                        String[] tmpProject = projectList.get(samePidIndex);
                        tmpProject[2] = String.valueOf(Long.valueOf(tmpProject[2]) + timeEntry.getDuration());
                        projectList.set(samePidIndex, tmpProject);
                    }
                } else {
                    if (0 < timeEntry.getDuration()) {
                        project[0] = pid;
                        project[1] = timeEntry.getDescription();
                        project[2] = String.valueOf(timeEntry.getDuration());
                        projectList.add(project);
                    }
                }
            }
        }
        return projectList;
    }

    private int getSamePidIndex(ArrayList<String[]> projectList, String pid) {
        for (int index = 0; index < projectList.size(); index++) {
            if (pid.equals(projectList.get(index)[0])) {
                return index;
            }
        }
        return -1;
    }

    public long getTotalDurations(ArrayList<String[]> projectList) {
        long totalDurations = 0;
        for (String[] project : projectList) {
            totalDurations = totalDurations + Long.valueOf(project[2]);
        }
        return totalDurations;
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

    public ArrayList<String> convertProjectList(ArrayList<String[]> tmpProjectList) {
        ArrayList<String> list = new ArrayList<>();
        for(String[] tmp : tmpProjectList){
            list.add(tmp[1] + ":  " + convertHms(Long.valueOf(tmp[2])));
        }
        return list;
    }

}
