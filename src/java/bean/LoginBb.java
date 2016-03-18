package bean;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import oauth.Twitter;

@Named
@RequestScoped
public class LoginBb extends SuperBb implements Serializable {

    @NotNull
    private String id;
    @NotNull
    private String pw;

    public String login() {
        HttpServletRequest request = getRequest();
        try {
            request.login(id, pw);
        } catch (ServletException ex) {
            facesErrorMsg("ログインできません");
            return "/loginError.xhtml?faces-redirect=true";
        }
        //Withings withings = new Withings();
        //withings.verifyWithings();
        //Zaim zaim = new Zaim();
        //zaim.verifyZaim();
        //Twitter twi = new Twitter();
        //twi.verifyTwitter(request, response);
        
        //responseComplete();
        return "/main/top.xhtml?faces-redirect=true";
    }

    public String logout() {
        getServlet().invalidateSession();
        HttpServletRequest request = getRequest();
        try {
            request.logout();
        } catch (ServletException ex) {

        }
        return "/login.xhtml?faces-redirect=true";
    }

    public void clear() {
        id = pw = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

}
