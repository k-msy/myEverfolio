/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.enterprise.context.Dependent;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bpg0129
 */
@Dependent
public class SuperBb implements Serializable {

    public ExternalContext getServlet() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

    public HttpServletRequest getRequest() {
        return (HttpServletRequest) getServlet().getRequest();
    }
    
    public HttpServletResponse getResponse(){
        return (HttpServletResponse) getServlet().getResponse();
    }
    
    public void responseComplete(){
        FacesContext.getCurrentInstance().responseComplete();
    }

    /* エラーメッセージを作成し、キューに入れる */
    public void facesErrorMsg(String s) {
        FacesMessage msg = new FacesMessage(s);
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    /* *****（メッセージを作成しキューに入れる）*****************
     FacesMessage.SEVERITY_FATAL		致命的エラー(4)
     FacesMessage.SEVERITY_ERROR		エラー(3)
     FacesMessage.SEVERITY_WARN		警告(2)
     FacesMessage.SEVERITY_WARN		情報(1)   
     *************************************************************/

    protected String URLEncode(String str) {
        try {
            str = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            System.out.println("パラメータ：そんなエンコードねぇよエラー");
        }
        return str;
    }
}
