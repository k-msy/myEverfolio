package bean;

import java.util.ArrayList;
import java.util.Date;
import javax.enterprise.context.SessionScoped;
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
@SessionScoped
public class HeaderBb extends SuperBb {

    private boolean zaimCoopFlg;
    private boolean zaim_coopEventExecute;
    private boolean wiCoopFlg;
    private boolean wi_coopEventExecute;
    private boolean todoCoopFlg;
    private boolean todo_coopEventExecute;

    @Inject
    Withings wi;
    @Inject
    Toggl toggl;
    @Inject
    Zaim zaim;
    @Inject
    Todoist todo;
    @Inject
    CalendarView cal;
    @Inject
    UtilDate utiDate;

    public String setRangeData() {
        Date start = cal.getFrom();
        Date end = cal.getTo();
        if (utiDate.isToday(start, end)) {
            return "/main/top.xhtml?faces-redirect=true";
        }
        ArrayList<String> dayList;
        dayList = utiDate.getDayList(start, end);
        int dayCount = dayList.size();
        String from = utiDate.formatYyyyMmDd(start);
        String to = utiDate.formatYyyyMmDd(end);
        if (wiCoopFlg) {
            HttpServletRequest request = getRequest();
            HttpSession session = request.getSession(true);
            if (wi.isExistAccessToken(session)) {
                wi.setRangeMeasures(from, to, dayList, dayCount);
            }
        }
        toggl.setRangeMeasures(start, end, dayList, dayCount);
        if (zaimCoopFlg) {
            zaim.setRangeMeasures(start, end, dayList, dayCount);
        }
        if (todoCoopFlg) {
            todo.setRangeMeasures(start, end, dayList, dayCount);
        }
        return "/main/summary.xhtml?faces-redirect=true";
    }

    public void zaimCoop() {
        HttpServletRequest request = getRequest();
        String eventExecute = request.getParameter("headerComp:zaim_coopEventExecute");
        if (null != eventExecute) switch (eventExecute) {
            case "true":
                zaimCoopFlg = zaim.changeCoop(zaimCoopFlg);
                break;
            case "false":
                zaimCoopFlg = zaim.cancelChangeCoop(zaimCoopFlg);
                break;
        }
    }

    public void wiCoop() {
        HttpServletRequest request = getRequest();
        String eventExecute = request.getParameter("headerComp:wi_coopEventExecute");
        if (null != eventExecute) switch (eventExecute) {
            case "true":
                wiCoopFlg = wi.changeCoop(wiCoopFlg);
                break;
            case "false":
                wiCoopFlg = wi.cancelChangeCoop(wiCoopFlg);
                break;
        }
    }

    public void todoCoop() {
        HttpServletRequest request = getRequest();
        String eventExecute = request.getParameter("headerComp:todo_coopEventExecute");
        if (null != eventExecute) switch (eventExecute) {
            case "true":
                todoCoopFlg = todo.changeCoop(todoCoopFlg);
                break;
            case "false":
                todoCoopFlg = todo.cancelChangeCoop(todoCoopFlg);
                break;
        }
    }

    public String logout() {
        return "";
    }
    
    

    public boolean isZaimCoopFlg() {
        return this.zaimCoopFlg;
    }

    public void setZaimCoopFlg(boolean zaimCoopFlg) {
        this.zaimCoopFlg = zaimCoopFlg;
    }

    public boolean isZaim_coopEventExecute() {
        return this.zaim_coopEventExecute;
    }

    public void setZaim_coopEventExecute(boolean zaim_coopEventExecute) {
        this.zaim_coopEventExecute = zaim_coopEventExecute;
    }

    public boolean isWiCoopFlg() {
        return this.wiCoopFlg;
    }

    public void setWiCoopFlg(boolean wiCoopFlg) {
        this.wiCoopFlg = wiCoopFlg;
    }

    public boolean isWi_coopEventExecute() {
        return this.wi_coopEventExecute;
    }

    public void setWi_coopEventExecute(boolean wi_coopEventExecute) {
        this.wi_coopEventExecute = wi_coopEventExecute;
    }

    public boolean isTodoCoopFlg() {
        return this.todoCoopFlg;
    }

    public void setTodoCoopFlg(boolean todoCoopFlg) {
        this.todoCoopFlg = todoCoopFlg;
    }

    public boolean isTodo_coopEventExecute() {
        return this.todo_coopEventExecute;
    }

    public void setTodo_coopEventExecute(boolean todo_coopEventExecute) {
        this.todo_coopEventExecute = todo_coopEventExecute;
    }
}
