/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import entity.TogglEnti;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import oauth.Toggl;
import oauth.Twitter;
import oauth.Withings;
import oauth.Zaim_origin;
import view.CalendarView;

/**
 *
 * @author bpg0129
 */
@Named
@RequestScoped
public class TopBb extends SuperBb implements Serializable {

    @Inject
    Withings wi;

    @Inject
    Toggl toggl;
    @Inject
    TogglEnti togEnti;
    private String[] project;
    
    @Inject
    CalendarView cal;

    @Inject
    Zaim_origin oZaim;

    public TopBb() {

    }

    public String verifyTwitter() {

        //MyManagedExecutorService 
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        //asyncExecute.start();

        Twitter twi = new Twitter();
        twi.verify(request, response);

        return "";
    }

    public String verifyZaim() {

        return "";
    }

    public String verifyWithings() {
        wi.verify();
        return "";
    }

    public String getAccessToken() {
        try {
            wi.getAccessToken();
        } catch (IOException ex) {
            Logger.getLogger(TopBb.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public String checkTokens() {
        HttpServletRequest request = getRequest();
        HttpSession session = request.getSession(true);

        if (wi.isExistAccessToken(session)) {
            try {
                // 歩数データを取得・設定する
                wi.setStepsMeasures();
                // 体重データを取得・設定する
                wi.setWeightMeasures();
            } catch (IOException ex) {
                System.out.println("歩数・体重データ取得でなんらかの例外キャッチしたよ");
            }
        }

        // Toggl
        try {
            ArrayList<String[]> tmpProjectList = toggl.getTodayDuration();

            long totalDurations = toggl.getTotalDurations(tmpProjectList);
            togEnti.setTotalDurations(toggl.convertHms(totalDurations));

            ArrayList<String> projectList = toggl.convertProjectList(tmpProjectList);
            togEnti.setProjectList(projectList);
        } catch (IOException e) {
            System.out.println("Toggl失敗");
            e.printStackTrace();
        }

        // Zaim
         /*
         try {
         oZaim.verify();
         } catch (IOException ex) {
         Logger.getLogger(TopBb.class.getName()).log(Level.SEVERE, null, ex);
         }
         */
        return "";
    }

    public String setRangeData() {
        //withings
        HttpServletRequest request = getRequest();
        HttpSession session = request.getSession(true);
        if (wi.isExistAccessToken(session)) {
            System.out.println("歩数・体重データを取得する準備ができてます");
            //try {
                // 歩数データを取得・設定する
                wi.setRangeStepsMeasures(cal.getFrom(), cal.getTo());
                // 体重データを取得・設定する
                wi.setRangeWeightMeasures(cal.getFrom(), cal.getTo());
            //} catch (IOException ex) {
            //    System.out.println("歩数・体重データ取得でなんらかの例外キャッチしたよ");
            //}
        }
        //toggl
        return "";
    }

}
