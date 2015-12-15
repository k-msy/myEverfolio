/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bean;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public TopBb() {
    }

    public String verifyTwitter() {
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        
        Twitter twi = new Twitter();
        twi.verify(request, response);

        return "";
    }
    
    public String verifyZaim(){
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        
        Zaim zaim = new Zaim();
        zaim.verify(request, response);        
               
        
        return "";
    }
    
    public String verifyWithings(){
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        
        Withings wi = new Withings();
        wi.verify(request, response);        
               
        
        return "";
    }
}
