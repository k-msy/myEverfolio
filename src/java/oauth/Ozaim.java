package oauth;

import static constants.Common.HTTP_GET;
import static constants.Const_oauth.*;
import static constants.Const_zaim.*;
import db.ZaimDb;
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
import thirdparty.zaim.Zaim;
import util.UtilLogic;

@RequestScoped
public class Ozaim extends SuperOauth {

    HttpServletRequest request = getRequest();
    HttpSession session = this.request.getSession(true);
    @EJB
    ZaimDb db;
    
    @Inject
    UtilLogic utiLogic;

    public void getRequestToken() throws IOException {
        String reqTokenResult;
        String callbackUrl = utiLogic.getAbsoluteContextPath(request) + ZA_RELATIVE_CALLBACK_URL;
        try {
            SortedMap<String, String> paramsMap = super.makeParam(CONSUMER_KEY, callbackUrl);
            String sigKey = super.makeSigKey(CONSUMER_SECRET, "");
            String sigData = super.makeSigData(CONSUMER_KEY, REQUEST_TOKEN_URL, paramsMap, HTTP_GET);
            
            reqTokenResult = super.getRequestToken(paramsMap, sigKey, sigData, callbackUrl, REQUEST_TOKEN_URL, HTTP_GET);
            if (!"".equals(reqTokenResult)) {
                setSessionValue(reqTokenResult);
                super.sendRedirect(CONSUMER_KEY, CONSUMER_SECRET, session.getAttribute(ZA_REQUEST_TOKEN).toString(), session.getAttribute(ZA_REQUEST_TOKEN_SECRET).toString(), AUTHORIZE_URL, HTTP_GET);
            } else {
                System.out.println("zaimリクエストトークン取得失敗");
            }
        } catch (IOException ex) {
            Logger.getLogger(Zaim.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    public void getAccessToken() throws IOException {
        String oauth_verifier = this.session.getAttribute(ZA_OAUTH_VERIFIER).toString();
        String oauth_token = this.session.getAttribute(ZA_OUTH_TOKEN).toString();
        String request_token_secret = this.session.getAttribute(ZA_REQUEST_TOKEN_SECRET).toString();
        SortedMap<String, String> paramsMap = new TreeMap();
        try {
            String sigData;
            String sigKey;

            paramsMap.put(OAUTH_CONSUMER_KEY, CONSUMER_KEY);
            paramsMap.put(OAUTH_VERIFIER, oauth_verifier);
            paramsMap.put(OAUTH_NONCE, super.getRandomChar());
            paramsMap.put(OAUTH_TIMESTAMP, String.valueOf(super.getUnixTime()));
            paramsMap.put(OAUTH_TOKEN, oauth_token);
            paramsMap.put(OAUTH_SIGNATURE_METHOD, HMAC_SHA1);
            paramsMap.put(OAUTH_VERSION, "1.0");

            sigData = super.makeSigData(CONSUMER_KEY, ACCESS_TOKEN_URL, paramsMap, HTTP_GET);
            sigKey = super.makeSigKey(CONSUMER_SECRET, request_token_secret);

            paramsMap.put(OAUTH_SIGNATURE, super.makeSignature(sigKey, sigData));

            URL url = new URL(
                    ACCESS_TOKEN_URL + "?"
                    + OAUTH_CONSUMER_KEY + "=" + URLEncode(paramsMap.get(OAUTH_CONSUMER_KEY)) + "&"
                    + OAUTH_VERIFIER + "=" + URLEncode(paramsMap.get(OAUTH_VERIFIER)) + "&"
                    + OAUTH_NONCE + "=" + URLEncode(paramsMap.get(OAUTH_NONCE)) + "&"
                    + OAUTH_SIGNATURE + "=" + URLEncode(paramsMap.get(OAUTH_SIGNATURE)) + "&"
                    + OAUTH_SIGNATURE_METHOD + "=" + URLEncode(paramsMap.get(OAUTH_SIGNATURE_METHOD)) + "&"
                    + OAUTH_TIMESTAMP + "=" + URLEncode(paramsMap.get(OAUTH_TIMESTAMP)) + "&"
                    + OAUTH_TOKEN + "=" + URLEncode(paramsMap.get(OAUTH_TOKEN)) + "&"
                    + OAUTH_VERSION + "=" + URLEncode(paramsMap.get(OAUTH_VERSION))
            );

            String[] split = extractToken(super.httpResponse(url, HTTP_GET));
            this.session.setAttribute(ZA_ACCESS_TOKEN, split[0]);
            this.session.setAttribute(ZA_ACCESS_TOKEN_SECRET, split[1]);
        } catch (IOException ex) {
            throw ex;
        }
    }

    public boolean isCallback(HttpSession session) {
        boolean flg = true;
        if ((session.getAttribute(ZA_OUTH_TOKEN) != null) && (session.getAttribute(ZA_OAUTH_VERIFIER) != null) && (session.getAttribute(ZA_REQUEST_TOKEN_SECRET) != null)) {
            try {
                getAccessToken();
                this.db.update(this.request, session);
                session.removeAttribute(ZA_OUTH_TOKEN);
                session.removeAttribute(ZA_OAUTH_VERIFIER);
                session.removeAttribute(ZA_REQUEST_TOKEN_SECRET);
                return true;
            } catch (IOException ex) {
                Logger.getLogger(Ozaim.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            flg = false;
        }
        return flg;
    }

    private void setSessionValue(String reqTokenResult) {
        String[] split = extractToken(reqTokenResult);
        session.setAttribute(ZA_REQUEST_TOKEN, split[0]);
        session.setAttribute(ZA_REQUEST_TOKEN_SECRET, split[1]);
    }
}
