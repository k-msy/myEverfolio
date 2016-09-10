package bean;

import java.io.Serializable;
import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Dependent
public class SuperBb implements Serializable {

    public ExternalContext getServlet() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

    public HttpServletRequest getRequest() {
        return (HttpServletRequest) getServlet().getRequest();
    }

    public HttpServletResponse getResponse() {
        return (HttpServletResponse) getServlet().getResponse();
    }

    public void responseComplete() {
        FacesContext.getCurrentInstance().responseComplete();
    }

    public void facesErrorMsg(String s) {
        FacesMessage msg = new FacesMessage(s);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
}
