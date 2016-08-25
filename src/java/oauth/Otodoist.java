package oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import db.TodoistDb;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
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
        String url = "https://todoist.com/oauth/authorize?client_id=22933d565398402a8e6fedd898c14d71&scope=data:read&state=" + super.getRandomChar();
        try {
            HttpServletResponse response = getResponse();
            response.sendRedirect(url);
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isCallback(HttpSession session) {
        if ((session.getAttribute("state") != null) && (session.getAttribute("code") != null)) {
            getAccessToken();
            this.db.update(this.request, session);
            this.db.insertKarmaDb(this.request, session);
            session.removeAttribute("state");
            session.removeAttribute("code");
            return true;
        }
        boolean flg = false;

        return flg;
    }

    private void getAccessToken() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String code = this.session.getAttribute("code").toString();
        try {
            URL url = new URL("https://todoist.com/oauth/access_token?client_id=22933d565398402a8e6fedd898c14d71&client_secret=0bb4307c73e54558a2ba8032143bf571&code=" + code);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String jsonText = reader.readLine();
            System.out.println("jsonText=" + jsonText);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = (JsonNode) mapper.readValue(jsonText, JsonNode.class);

            String[] accessTokenStr = node.get("access_token").toString().split("\"");
            String accessToken = accessTokenStr[1];

            String[] token_typeStr = node.get("token_type").toString().split("\"");
            String token_type = token_typeStr[1];

            this.session.setAttribute("access_token", accessToken);
            this.session.setAttribute("token_type", token_type);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Otodoist.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(Otodoist.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Otodoist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
