package oauth;

import db.WithingsDb;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RequestScoped
public class Owithings extends SuperOauth {

    @EJB
    WithingsDb db;
    private static final String method = "GET";
    HttpServletRequest request = getRequest();
    HttpSession session = this.request.getSession(true);

    public boolean isCallback(HttpSession session) {
        boolean flg = true;
        if ((session.getAttribute("wi_oauth_token") != null) && (session.getAttribute("wi_request_token_secret") != null)) {
            try {
                getAccessToken();
                this.db.update(this.request, session);

                session.removeAttribute("wi_userId");
                session.removeAttribute("wi_oauth_token");
                session.removeAttribute("wi_request_token_secret");
                return true;
            } catch (IOException ex) {
                Logger.getLogger(Ozaim.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            flg = false;
        }
        return flg;
    }

    public void getAccessToken()
            throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String oauth_token = this.session.getAttribute("wi_oauth_token").toString();
        String request_token_secret = this.session.getAttribute("wi_request_token_secret").toString();
        try {
            SortedMap<String, String> paramsMap = new TreeMap();

            paramsMap.put("oauth_consumer_key", "f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f");
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", oauth_token);
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_version", "1.0");

            String sigData = super.makeSigData("f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f", "https://oauth.withings.com/account/access_token", paramsMap, "GET");
            String sigKey = super.makeSigKey("c17484ff357d801897828f674bb6175b4f94340516ce5bb4922def7e035", request_token_secret);

            paramsMap.put("oauth_signature", super.makeSignature(sigKey, sigData));

            URL url = new URL("https://oauth.withings.com/account/access_token?oauth_consumer_key=" + URLEncode((String) paramsMap.get("oauth_consumer_key")) + "&oauth_nonce=" + URLEncode((String) paramsMap.get("oauth_nonce")) + "&oauth_signature=" + URLEncode((String) paramsMap.get("oauth_signature")) + "&oauth_signature_method=" + URLEncode((String) paramsMap.get("oauth_signature_method")) + "&oauth_timestamp=" + URLEncode((String) paramsMap.get("oauth_timestamp")) + "&oauth_token=" + URLEncode((String) paramsMap.get("oauth_token")) + "&oauth_version=" + URLEncode((String) paramsMap.get("oauth_version")));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String[] split = extractToken(reader.readLine());
            System.out.println("access_token=" + split[0]);
            System.out.println("access_token_secret=" + split[1]);
            this.session.setAttribute("wi_access_token", split[0]);
            this.session.setAttribute("wi_access_token_secret", split[1]);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void getRequestToken() {
        String reqTokenResult = "";
        try {
            SortedMap<String, String> paramsMap = super.makeParam("f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f", "http://127.0.0.1:8080/myEverfolio/faces/main/callback/withings");
            String sigKey = super.makeSigKey("c17484ff357d801897828f674bb6175b4f94340516ce5bb4922def7e035", "");
            String sigData = super.makeSigData("f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f", "https://oauth.withings.com/account/request_token", paramsMap, "GET");

            reqTokenResult = getRequestToken(paramsMap, sigKey, sigData, "http://127.0.0.1:8080/myEverfolio/faces/main/callback/withings", "https://oauth.withings.com/account/request_token", "GET");
            if (!"".equals(reqTokenResult)) {
                String[] split = extractToken(reqTokenResult);
                this.session.setAttribute("wi_request_token", split[0]);
                this.session.setAttribute("wi_request_token_secret", split[1]);

                String request_token = this.session.getAttribute("wi_request_token").toString();
                String request_token_secret = this.session.getAttribute("wi_request_token_secret").toString();

                super.sendRedirect("f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f", "c17484ff357d801897828f674bb6175b4f94340516ce5bb4922def7e035", request_token, request_token_secret, "https://oauth.withings.com/account/authorize", "GET");
            } else {
                System.out.println("withingsリクエストトークン失敗");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
