package bean;

import static constants.Common.USER_ID;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

@Named
@RequestScoped
public class LoginBb extends SuperBb implements Serializable {

    @NotNull
    private String id;
    @NotNull
    private String pw;

    /**
     * ログイン
     * @return 
     */
    public String login() {
        HttpServletRequest request = getRequest();
        try {
            request.login(id, pw);
            HttpSession session = request.getSession(true);
            session.setAttribute(USER_ID, id);
                        
        } catch (ServletException ex) {
            facesErrorMsg("ログイン失敗");
            return "/loginError.xhtml?faces-redirect=true";
        }
        return "/main/top.xhtml?faces-redirect=true";
    }

    /**
     * ログアウト
     * @return 
     */
    public String logout() {
        getServlet().invalidateSession();
        HttpServletRequest request = getRequest();
        try {
            request.logout();
        } catch (ServletException localServletException) {
        }
        return "/login.xhtml?faces-redirect=true";
    }

    /**
     * クリア
     */
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
