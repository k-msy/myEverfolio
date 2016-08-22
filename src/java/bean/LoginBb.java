package bean;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

@Named
@RequestScoped
public class LoginBb
        extends SuperBb
        implements Serializable {

    @NotNull
    private String id;
    @NotNull
    private String pw;

    public String login() {
        HttpServletRequest request = getRequest();
        try {
            request.login(this.id, this.pw);
            HttpSession session = request.getSession(true);
            session.setAttribute("user_id", this.id);
        } catch (ServletException ex) {
            facesErrorMsg("ログイン失敗");
            return "/loginError.xhtml?faces-redirect=true";
        }
        return "/main/top.xhtml?faces-redirect=true";
    }

    public String logout() {
        getServlet().invalidateSession();
        HttpServletRequest request = getRequest();
        try {
            request.logout();
        } catch (ServletException localServletException) {
        }
        return "/login.xhtml?faces-redirect=true";
    }

    public void clear() {
        this.id = (this.pw = null);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return this.pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }
}
