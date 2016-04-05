/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import entity.TogglEnti;
import entity.WithingsEnti;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import oauth.Toggl;
import oauth.Twitter;
import oauth.Withings;
import oauth.Zaim;

/**
 *
 * @author bpg0129
 */
@Named
@RequestScoped
public class TopBb extends SuperBb implements Serializable {

    @EJB
    private AsyncExecute asyncExecute;

    @Inject
    Withings wi;
    @Inject
    WithingsEnti wiEnti;
    
    @Inject
    Toggl toggl;
    @Inject
    TogglEnti togEnti;
    private String[] project;


    public TopBb() {
        wi = new Withings();
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
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();

        Zaim zaim = new Zaim();
        zaim.verify(request, response);

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
/*
        // Withings
        if (session.getAttribute("request_token") == null) {
            wi.verify();
        } else if (session.getAttribute("access_token") == null) {
            try {
                wi.getAccessToken();
            } catch (IOException ex) {
                Logger.getLogger(TopBb.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("歩数・体重データを取得する準備ができてます");
            try {
                // 歩数・体重データを取得する準備ができてます。
                String rawDataForSteps = wi.getRawDataForSteps();
                Map<String, String> stepsMap = new HashMap<>();
                stepsMap = wi.adjustSteps(rawDataForSteps);
                wiEnti.setYesterday(stepsMap.get("yesterday"));
                wiEnti.setToday(stepsMap.get("today"));
                wiEnti.setDifference(stepsMap.get("difference"));
                //System.out.println(wi.getRawDataForSteps());
            } catch (IOException ex) {
                Logger.getLogger(TopBb.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
*/        
        // Toggl
        try{
            ArrayList<String[]> tmpProjectList = toggl.getTodayDuration();

            long totalDurations = toggl.getTotalDurations(tmpProjectList);
            togEnti.setTotalDurations(toggl.convertHms(totalDurations));
            
            ArrayList<String> projectList = toggl.convertProjectList(tmpProjectList);
            togEnti.setProjectList(projectList);
        }catch(IOException e){
            System.out.println("Toggl失敗");
            e.printStackTrace();
        }
        
        
        

        return "";
    }
}
