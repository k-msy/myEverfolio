package oauth;

import com.sun.xml.wss.impl.misc.Base64;
import static constants.Common.UTF8;
import static constants.Const_oauth.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import thirdparty.withings.Withings;

public class SuperOauth
        extends HttpServlet {

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

    public void sendRedirect(HttpServletResponse response, String url, String token)
            throws IOException {
        response.setContentType("text/html; charset=" + UTF8);
        String redirectUrl = url + "?" + OAUTH_TOKEN + "=" + token;
        response.sendRedirect(redirectUrl);
    }

    protected String makeSignature(String sigKey, String sigData) {
        byte[] rawHmac = null;

        SecretKeySpec signingKey = new SecretKeySpec(sigKey.getBytes(), "HmacSHA1");
        try {
            Mac mac = Mac.getInstance(signingKey.getAlgorithm());
            try {
                mac.init(signingKey);
                rawHmac = mac.doFinal(sigData.getBytes());
            } catch (InvalidKeyException ex) {
                System.out.println("InvalidKeyException!!");
            }
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("該当するアルゴリズムがありません");
        }
        return Base64.encode(rawHmac);
    }

    protected String makeAuthHeader(SortedMap<String, String> paramsMap) {
        String paramStr = "";
        for (Map.Entry<String, String> param : paramsMap.entrySet()) {
            paramStr = paramStr + ", " + (String) param.getKey() + "=\"" + URLEncode((String) param.getValue()) + "\"";
        }
        paramStr = paramStr.substring(2);

        return "OAuth " + paramStr;
    }

    protected String makeSigKey(String consumer_secret, String token_secret) {
        return URLEncode(consumer_secret) + "&" + URLEncode(token_secret);
    }

    protected String makeSigData(String consumer_key, String URL, SortedMap<String, String> paramsMap, String method) {
        Iterator entries = paramsMap.entrySet().iterator();
        String params = "";
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            params = params + (String) entry.getKey() + "=" + (String) entry.getValue() + "&";
        }
        params = params.substring(0, params.length() - 1);
        params = URLEncode(params);
        String requestURL = URLEncode(URL);
        return method + "&" + requestURL + "&" + params;
    }

    protected SortedMap<String, String> makeParam(String consumer_key, String oauth_callback) {
        SortedMap<String, String> paramsMap = new TreeMap();
        paramsMap.put(OAUTH_CALLBACK, URLEncode(oauth_callback));
        paramsMap.put(OAUTH_CONSUMER_KEY, consumer_key);
        paramsMap.put(OAUTH_NONCE, getRandomChar());
        paramsMap.put(OAUTH_TIMESTAMP, String.valueOf(getUnixTime()));
        paramsMap.put(OAUTH_SIGNATURE_METHOD, HMAC_SHA1);
        paramsMap.put(OAUTH_VERSION, "1.0");

        return paramsMap;
    }

    /**
     *
     * @param paramsMap
     * @param sigKey
     * @param sigData
     * @param oauth_callback
     * @param REQUEST_TOKEN_URL
     * @param method
     * @return
     * @throws IOException
     */
    protected String getRequestToken(SortedMap<String, String> paramsMap, String sigKey, String sigData, String oauth_callback, String REQUEST_TOKEN_URL, String method) throws IOException {
        try {
            paramsMap.put(OAUTH_SIGNATURE, makeSignature(sigKey, sigData));
            URL url = new URL(
                    REQUEST_TOKEN_URL + "?"
                    + OAUTH_CALLBACK + "=" + URLEncode(oauth_callback) + "&"
                    + OAUTH_CONSUMER_KEY + "=" + URLEncode(paramsMap.get(OAUTH_CONSUMER_KEY)) + "&"
                    + OAUTH_NONCE + "=" + URLEncode(paramsMap.get(OAUTH_NONCE)) + "&"
                    + OAUTH_SIGNATURE + "=" + URLEncode(paramsMap.get(OAUTH_SIGNATURE)) + "&"
                    + OAUTH_SIGNATURE_METHOD + "=" + URLEncode(paramsMap.get(OAUTH_SIGNATURE_METHOD)) + "&"
                    + OAUTH_TIMESTAMP + "=" + URLEncode(paramsMap.get(OAUTH_TIMESTAMP)) + "&"
                    + OAUTH_VERSION + "=" + URLEncode(paramsMap.get(OAUTH_VERSION))
            );
            return this.httpResponse(url, method);
        } catch (IOException e) {
            if ((e instanceof IOException)) {
                e.printStackTrace();
            } else {
                System.out.println("otherException");
            }
        }
        return "";
    }

    protected void sendRedirect(String CONSUMER_KEY, String CONSUMER_SECRET, String REQUEST_TOKEN, String REQUEST_TOKEN_SECRET, String AUTHORIZE_URL, String method) {
        SortedMap<String, String> paramsMap = new TreeMap();
        paramsMap.put(OAUTH_CONSUMER_KEY, CONSUMER_KEY);
        paramsMap.put(OAUTH_NONCE, getRandomChar());
        paramsMap.put(OAUTH_TIMESTAMP, String.valueOf(getUnixTime()));
        paramsMap.put(OAUTH_TOKEN, REQUEST_TOKEN);
        paramsMap.put(OAUTH_SIGNATURE_METHOD, HMAC_SHA1);
        paramsMap.put(OAUTH_VERSION, "1.0");

        String sigData = makeSigData(CONSUMER_KEY, AUTHORIZE_URL, paramsMap, method);
        String sigKey = makeSigKey(CONSUMER_SECRET, REQUEST_TOKEN_SECRET);

        String redirectUrl = AUTHORIZE_URL + "?"
                + OAUTH_CONSUMER_KEY + "=" + URLEncode(paramsMap.get(OAUTH_CONSUMER_KEY)) + "&"
                + OAUTH_NONCE + "=" + URLEncode(paramsMap.get(OAUTH_NONCE)) + "&"
                + OAUTH_SIGNATURE + "=" + URLEncode(makeSignature(sigKey, sigData)) + "&"
                + OAUTH_SIGNATURE_METHOD + "=" + URLEncode(paramsMap.get(OAUTH_SIGNATURE_METHOD)) + "&"
                + OAUTH_TIMESTAMP + "=" + URLEncode(paramsMap.get(OAUTH_TIMESTAMP)) + "&"
                + OAUTH_TOKEN + "=" + URLEncode(paramsMap.get(OAUTH_TOKEN)) + "&"
                + OAUTH_VERSION + "=" + URLEncode(paramsMap.get(OAUTH_VERSION));
        try {
            HttpServletResponse response = getResponse();
            response.sendRedirect(redirectUrl);
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected int getUnixTime() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    protected String getRandomChar() {
        return RandomStringUtils.randomAlphanumeric(32);
    }

    protected String URLEncode(String str) {
        try {
            str = URLEncoder.encode(str, UTF8);
        } catch (UnsupportedEncodingException ex) {
            System.out.println("UnsupportedEncodingException");
        }
        return str;
    }

    protected String[] extractToken(String chars) {
        String[] result = null;
        try {
            result = chars.split("&");
            for (int i = 0; i < result.length; i++) {
                String[] a = result[i].split("=");
                result[i] = a[1];
            }
        } catch (PatternSyntaxException ex) {
            System.out.println("PatternSyntaxException");
        }
        if ((result == null) || (result.length == 0)) {
            result[0] = "";
        }
        return result;
    }

    protected String httpResponse(URL url, String httpMethod) throws ProtocolException, IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(httpMethod);
        connection.connect();
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return reader.readLine();
    }

}
