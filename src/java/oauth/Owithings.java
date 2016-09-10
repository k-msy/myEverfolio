package oauth;

import static constants.Common.HTTP_GET;
import static constants.Const_oauth.*;
import static constants.Const_withings.*;
import db.WithingsDb;
import java.io.IOException;
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
import util.UtilLogic;

@RequestScoped
public class Owithings extends SuperOauth {

    @EJB
    WithingsDb db;

    @Inject
    UtilLogic utiLogic;

    HttpServletRequest request = getRequest();
    HttpSession session = this.request.getSession(true);

    public boolean isCallback(HttpSession session) {
        boolean flg = true;
        if ((session.getAttribute(WI_OAUTH_TOKEN) != null) && (session.getAttribute(WI_REQUEST_TOKEN_SECRET) != null)) {
            try {
                getAccessToken();
                this.db.update(this.request, session);

                session.removeAttribute(WI_USERID);
                session.removeAttribute(WI_OAUTH_TOKEN);
                session.removeAttribute(WI_REQUEST_TOKEN_SECRET);
                return true;
            } catch (IOException ex) {
                Logger.getLogger(Ozaim.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            flg = false;
        }
        return flg;
    }

    public void getAccessToken() throws IOException {
        String oauth_token = this.session.getAttribute(WI_OAUTH_TOKEN).toString();
        String request_token_secret = this.session.getAttribute(WI_REQUEST_TOKEN_SECRET).toString();
        try {
            SortedMap<String, String> paramsMap = new TreeMap();
            paramsMap.put(OAUTH_CONSUMER_KEY, CONSUMER_KEY);
            paramsMap.put(OAUTH_NONCE, super.getRandomChar());
            paramsMap.put(OAUTH_TIMESTAMP, String.valueOf(super.getUnixTime()));
            paramsMap.put(OAUTH_TOKEN, oauth_token);
            paramsMap.put(OAUTH_SIGNATURE_METHOD, HMAC_SHA1);
            paramsMap.put(OAUTH_VERSION, "1.0");

            String sigData = super.makeSigData(CONSUMER_KEY, ACCESS_TOKEN_URL, paramsMap, HTTP_GET);
            String sigKey = super.makeSigKey(CONSUMER_SECRET, request_token_secret);

            paramsMap.put(OAUTH_SIGNATURE, super.makeSignature(sigKey, sigData));

            URL url = new URL(
                    ACCESS_TOKEN_URL + "?"
                    + OAUTH_CONSUMER_KEY + "=" + URLEncode((String) paramsMap.get(OAUTH_CONSUMER_KEY)) + "&"
                    + OAUTH_NONCE + "=" + URLEncode(paramsMap.get(OAUTH_NONCE)) + "&"
                    + OAUTH_SIGNATURE + "=" + URLEncode(paramsMap.get(OAUTH_SIGNATURE)) + "&"
                    + OAUTH_SIGNATURE_METHOD + "=" + URLEncode(paramsMap.get(OAUTH_SIGNATURE_METHOD)) + "&"
                    + OAUTH_TIMESTAMP + "=" + URLEncode((String) paramsMap.get(OAUTH_TIMESTAMP)) + "&"
                    + OAUTH_TOKEN + "=" + URLEncode((String) paramsMap.get(OAUTH_TOKEN)) + "&"
                    + OAUTH_VERSION + "=" + URLEncode((String) paramsMap.get(OAUTH_VERSION))
            );
            String[] split = extractToken(super.httpResponse(url, HTTP_GET));
            this.session.setAttribute(WI_ACCESS_TOKEN, split[0]);
            this.session.setAttribute(WI_ACCESS_TOKEN_SECRET, split[1]);
        } catch (IOException ex) {

        }
    }

    public void getRequestToken() {
        String reqTokenResult;
        String callbackUrl = utiLogic.getAbsoluteContextPath(request) + WI_RELATIVE_CALLBACK_URL;
        try {
            SortedMap<String, String> paramsMap = super.makeParam(CONSUMER_KEY, callbackUrl);
            String sigKey = super.makeSigKey(CONSUMER_SECRET, "");
            String sigData = super.makeSigData(CONSUMER_KEY, REQUEST_TOKEN_URL, paramsMap, HTTP_GET);

            reqTokenResult = getRequestToken(paramsMap, sigKey, sigData, callbackUrl, REQUEST_TOKEN_URL, HTTP_GET);
            if (!"".equals(reqTokenResult)) {
                String[] split = extractToken(reqTokenResult);
                this.session.setAttribute(WI_REQUEST_TOKEN, split[0]);
                this.session.setAttribute(WI_REQUEST_TOKEN_SECRET, split[1]);

                String request_token = this.session.getAttribute(WI_REQUEST_TOKEN).toString();
                String request_token_secret = this.session.getAttribute(WI_REQUEST_TOKEN_SECRET).toString();

                super.sendRedirect(CONSUMER_KEY, CONSUMER_SECRET, request_token, request_token_secret, AUTHORIZE_URL, HTTP_GET);
            } else {
                System.out.println("withingsリクエストトークン失敗");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
