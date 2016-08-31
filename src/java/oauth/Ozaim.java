package oauth;

import db.ZaimDb;
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
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import thirdparty.zaim.Zaim;
import util.UtilLogic;

@RequestScoped
public class Ozaim extends SuperOauth {

    private static final String method = "GET";
    HttpServletRequest request = getRequest();
    HttpSession session = this.request.getSession(true);
    @EJB
    ZaimDb db;
    
    @Inject
    UtilLogic utiLogic;

    public void getRequestToken() throws IOException {
        String reqTokenResult = "";
        String callbackUrl = utiLogic.getAbsoluteContextPath(request) + "/faces/main/callback/zaim";
        try {
            SortedMap<String, String> paramsMap = super.makeParam("b12800bf82cfe709683c9b812d35fc450efb8bc4", callbackUrl);
            String sigKey = super.makeSigKey("6a1b7a0ad40bdbd3d9b4d0a64fb757d10a606af4", "");
            String sigData = super.makeSigData("b12800bf82cfe709683c9b812d35fc450efb8bc4", "https://api.zaim.net/v2/auth/request", paramsMap, "GET");

            
            
            reqTokenResult = super.getRequestToken(paramsMap, sigKey, sigData, callbackUrl, "https://api.zaim.net/v2/auth/request", "GET");
            if (!"".equals(reqTokenResult)) {
                String[] split = extractToken(reqTokenResult);
                this.session.setAttribute("za_request_token", split[0]);
                this.session.setAttribute("za_request_token_secret", split[1]);

                String request_token = this.session.getAttribute("za_request_token").toString();
                String request_token_secret = this.session.getAttribute("za_request_token_secret").toString();

                super.sendRedirect("b12800bf82cfe709683c9b812d35fc450efb8bc4", "6a1b7a0ad40bdbd3d9b4d0a64fb757d10a606af4", request_token, request_token_secret, "https://auth.zaim.net/users/auth", "GET");
            } else {
                System.out.println("zaimリクエストトークン取得失敗");
            }
        } catch (IOException ex) {
            Logger.getLogger(Zaim.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    public void getAccessToken()
            throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String oauth_verifier = this.session.getAttribute("za_oauth_verifier").toString();
        String oauth_token = this.session.getAttribute("za_oauth_token").toString();
        String request_token_secret = this.session.getAttribute("za_request_token_secret").toString();
        SortedMap<String, String> paramsMap = new TreeMap();
        try {
            String sigData = "";
            String sigKey = "";

            paramsMap.put("oauth_consumer_key", "b12800bf82cfe709683c9b812d35fc450efb8bc4");
            paramsMap.put("oauth_verifier", oauth_verifier);
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", oauth_token);
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_version", "1.0");

            sigData = super.makeSigData("b12800bf82cfe709683c9b812d35fc450efb8bc4", "https://api.zaim.net/v2/auth/access", paramsMap, "GET");
            sigKey = super.makeSigKey("6a1b7a0ad40bdbd3d9b4d0a64fb757d10a606af4", request_token_secret);

            paramsMap.put("oauth_signature", super.makeSignature(sigKey, sigData));

            URL url = new URL("https://api.zaim.net/v2/auth/access?oauth_consumer_key=" + URLEncode((String) paramsMap.get("oauth_consumer_key")) + "&oauth_verifier=" + URLEncode((String) paramsMap.get("oauth_verifier")) + "&oauth_nonce=" + URLEncode((String) paramsMap.get("oauth_nonce")) + "&oauth_signature=" + URLEncode((String) paramsMap.get("oauth_signature")) + "&oauth_signature_method=" + URLEncode((String) paramsMap.get("oauth_signature_method")) + "&oauth_timestamp=" + URLEncode((String) paramsMap.get("oauth_timestamp")) + "&oauth_token=" + URLEncode((String) paramsMap.get("oauth_token")) + "&oauth_version=" + URLEncode((String) paramsMap.get("oauth_version")));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String[] split = extractToken(reader.readLine());
            System.out.println("za_access_token=" + split[0]);
            System.out.println("za_access_token_secret=" + split[1]);
            this.session.setAttribute("za_access_token", split[0]);
            this.session.setAttribute("za_access_token_secret", split[1]);
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Logger.getLogger(Zaim.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean isCallback(HttpSession session) {
        boolean flg = true;
        if ((session.getAttribute("za_oauth_token") != null) && (session.getAttribute("za_oauth_verifier") != null) && (session.getAttribute("za_request_token_secret") != null)) {
            try {
                getAccessToken();
                this.db.update(this.request, session);

                session.removeAttribute("za_oauth_token");
                session.removeAttribute("za_oauth_verifier");
                session.removeAttribute("za_request_token_secret");
                return true;
            } catch (IOException ex) {
                Logger.getLogger(Ozaim.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            flg = false;
        }
        return flg;
    }
}
