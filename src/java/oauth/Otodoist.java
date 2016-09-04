package oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static constants.Common.HTTP_POST;
import static constants.Const_todoist.*;
import db.TodoistDb;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import thirdparty.withings.Withings;

@RequestScoped
public class Otodoist extends SuperOauth {

    HttpServletRequest request = getRequest();
    HttpSession session = this.request.getSession(true);
    @EJB
    TodoistDb db;

    public void getRequestToken() {
        String url = AUTHORIZATION_URL + "?" + TODO_CLIENT_ID + "=" + CLIENT_ID + "&" + TODO_SCOPE + "=" + TODO_DATA_READ + "&" + TODO_STATE + "=" + super.getRandomChar();
        try {
            HttpServletResponse response = getResponse();
            response.sendRedirect(url);
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isCallback(HttpSession session) {
        if ((session.getAttribute(TODO_STATE) != null) && (session.getAttribute(TODO_CODE) != null)) {
            getAccessToken();
            db.update(request, session);
            db.insertKarmaDb(request, session);
            session.removeAttribute(TODO_STATE);
            session.removeAttribute(TODO_CODE);
            return true;
        }
        boolean flg = false;
        return flg;
    }

    private void getAccessToken() {
        String code = session.getAttribute(TODO_CODE).toString();
        try {
            URL url = new URL(
                    ACCESS_TOKEN_URL + "?"
                    + TODO_CLIENT_ID + "=" + CLIENT_ID + "&"
                    + TODO_CLIENT_SECRET + "=" + CLIENT_SECRET + "&"
                    + TODO_CODE + "=" + code
            );
           
            String jsonText = super.httpResponse(url, HTTP_POST);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);

            String[] accessTokenStr = node.get(TODO_ACCESS_TOKEN).toString().split("\"");
            String[] token_typeStr = node.get(TODO_TOKEN_TYPE).toString().split("\"");

            session.setAttribute(TODO_ACCESS_TOKEN, accessTokenStr[1]);
            session.setAttribute(TODO_TOKEN_TYPE, token_typeStr[1]);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Otodoist.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(Otodoist.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Otodoist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
