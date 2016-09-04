package bean;

import entity.TogglEnti;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import thirdparty.todoist.Todoist;
import thirdparty.toggl.Toggl;
import thirdparty.withings.Withings;
import thirdparty.zaim.Zaim;
import util.UtilDate;
import view.CalendarView;

@Named
@RequestScoped
public class TopBb extends SuperBb implements Serializable {

    @Inject
    Withings wi;

    @Inject
    Toggl toggl;

    @Inject
    TogglEnti togEnti;

    @Inject
    Todoist todo;

    @Inject
    CalendarView cal;

    @Inject
    UtilDate utiDate;

    @Inject
    Zaim zaim;

    public String checkTokens() {
        HttpServletRequest request = getRequest();
        HttpSession session = request.getSession(true);

        Date today = utiDate.convertLocalDateTimeToDate(LocalDate.now().atTime(0, 0, 0));
        if (cal.getFrom() == null) {
            cal.setFrom(today);
        }
        if (cal.getTo() == null) {
            cal.setTo(today);
        }

        //withings
        if (wi.doesCooperate(session)) {
            try {
                wi.setStepsMeasures();
                wi.setWeightMeasures();
            } catch (IOException ex) {
                System.out.println("歩数・体重データ取得でなんらかの例外をキャッチしたよ");
            }
        }

        //toggl
        try {
            ArrayList<String[]> tmpProjectList = toggl.getTodayDuration();

            long totalDurations = toggl.getTotalDurations(tmpProjectList);
            togEnti.setTotalDurations(toggl.convertHms(totalDurations));

            ArrayList<String> projectList = this.toggl.convertProjectList(tmpProjectList);
            togEnti.setProjectList(projectList);
        } catch (IOException e) {
            System.out.println("Toggl失敗");
            e.printStackTrace();
        }

        //zaim
        if (this.zaim.doesCooperate(session)) {
            this.zaim.setMoneyMeasures();
        }

        //todoist
        if (this.todo.doesCooperate(session)) {
            this.todo.setTaskMeasures();
            this.todo.syncKarmaData();
        }
        return "";
    }
}
