/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oauth;

import com.sun.xml.wss.impl.misc.Base64;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author bpg0129
 */
public class SuperOauth extends HttpServlet {

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

    public void sendRedirect(HttpServletResponse response, String url, String token) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        //PrintWriter out = response.getWriter();
        //log("アクセスされました");
        String redirectUrl = url + "?oauth_token=" + token;
        //System.out.println("redirectUrl：" + redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    protected String makeSignature(String sigKey, String sigData) {
        byte[] rawHmac = null;
        Mac mac;
        SecretKeySpec signingKey = new SecretKeySpec(sigKey.getBytes(), "HmacSHA1");
        try {
            mac = Mac.getInstance(signingKey.getAlgorithm());
            try {
                mac.init(signingKey);
                rawHmac = mac.doFinal(sigData.getBytes());
            } catch (InvalidKeyException ex) {
                System.out.println("InvalidKeyException!!");
            }
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("そんなアルゴリズムねぇよエラー");
        }
        return Base64.encode(rawHmac);
    }

    /**
     * Authorizationヘッダの作成
     *
     * @param paramsMap
     * @return
     */
    protected String makeAuthHeader(SortedMap<String, String> paramsMap) {
        String paramStr = "";
        for (Map.Entry<String, String> param : paramsMap.entrySet()) {
            paramStr += ", " + param.getKey() + "=\""
                    + URLEncode(param.getValue()) + "\"";
        }
        paramStr = paramStr.substring(2);

        return "OAuth " + paramStr;
    }
    

    /**
     * signatureKeyを作成する
     *
     * @param consumer_secret
     * @param token_secret
     * @return
     */
    protected String makeSigKey(String consumer_secret, String token_secret) {
        // 1. consumer_secretをURLエンコード 
        // 2. oauth_token_secretをURLエンコード
        // 3. 1と2を"&"で連結
        //System.out.println("sigKey: " + URLEncode(consumer_secret) + "&" + URLEncode(token_secret));
        return URLEncode(consumer_secret) + "&" + URLEncode(token_secret);
    }

    /**
     * signatureDataを作成する
     *
     * @param consumer_key
     * @param URL
     * @param paramsMap
     * @param method
     * @return
     */
    protected String makeSigData(String consumer_key, String URL, SortedMap<String, String> paramsMap, String method) {

        Iterator entries = paramsMap.entrySet().iterator();
        String params = "";
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            params = params + (String) entry.getKey() + "=" + (String) entry.getValue() + "&";
        }
        params = params.substring(0, params.length() - 1);

        // 2. パラメータをURLエンコード化する
        params = URLEncode(params);

        // 3. リクエストメソッド
        //String method = "GET";
        // 4. リクエストURLをエンコード
        String requestURL = URLEncode(URL);

        //System.out.println("base string: " + method + "&" + requestURL + "&" + params);
        return method + "&" + requestURL + "&" + params;
    }

    //認証で使う共通のパラメータを生成する
    protected SortedMap<String, String> makeParam(String consumer_key) {
        SortedMap<String, String> paramsMap;
        paramsMap = new TreeMap<>();
        paramsMap.put("oauth_consumer_key", consumer_key);
        paramsMap.put("oauth_nonce", getRandomChar());
        paramsMap.put("oauth_timestamp", String.valueOf(getUnixTime()));
        paramsMap.put("oauth_signature_method", "HMAC-SHA1");
        paramsMap.put("oauth_version", "1.0");

        return paramsMap;
    }

    /**
     *
     * @return
     */
    protected int getUnixTime() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    /**
     * 半角英数ランダムな32文字を取得
     *
     * @return
     */
    protected String getRandomChar() {
        return RandomStringUtils.randomAlphanumeric(32);
    }

    protected String URLEncode(String str) {
        try {
            str = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            System.out.println("パラメータ：そんなエンコードねぇよエラー");
        }
        return str;
    }
}
