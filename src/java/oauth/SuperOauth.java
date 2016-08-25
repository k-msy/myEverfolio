package oauth;

import com.sun.xml.wss.impl.misc.Base64;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
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
        response.setContentType("text/html; charset=UTF-8");
        String redirectUrl = url + "?oauth_token=" + token;
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
            System.out.println("���������������������������������������������");
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
        paramsMap.put("oauth_callback", URLEncode(oauth_callback));
        paramsMap.put("oauth_consumer_key", consumer_key);
        paramsMap.put("oauth_nonce", getRandomChar());
        paramsMap.put("oauth_timestamp", String.valueOf(getUnixTime()));
        paramsMap.put("oauth_signature_method", "HMAC-SHA1");
        paramsMap.put("oauth_version", "1.0");

        return paramsMap;
    }

    protected String getRequestToken(SortedMap<String, String> paramsMap, String sigKey, String sigData, String OAUTH_CALLBACK, String REQUEST_TOKEN_URL, String method)
            throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            paramsMap.put("oauth_signature", makeSignature(sigKey, sigData));

            URL url = new URL(REQUEST_TOKEN_URL + "?oauth_callback=" + URLEncode(OAUTH_CALLBACK) + "&oauth_consumer_key=" + URLEncode((String) paramsMap.get("oauth_consumer_key")) + "&oauth_nonce=" + URLEncode((String) paramsMap.get("oauth_nonce")) + "&oauth_signature=" + URLEncode((String) paramsMap.get("oauth_signature")) + "&oauth_signature_method=" + URLEncode((String) paramsMap.get("oauth_signature_method")) + "&oauth_timestamp=" + URLEncode((String) paramsMap.get("oauth_timestamp")) + "&oauth_version=" + URLEncode((String) paramsMap.get("oauth_version")));

            System.out.println("url=" + url.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            if ((e instanceof IOException)) {
                e.printStackTrace();
            } else {
                System.out.println("otherException");
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return "";
    }

    protected void sendRedirect(String CONSUMER_KEY, String CONSUMER_SECRET, String REQUEST_TOKEN, String REQUEST_TOKEN_SECRET, String AUTHORIZE_URL, String method) {
        SortedMap<String, String> paramsMap = new TreeMap();
        paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
        paramsMap.put("oauth_nonce", getRandomChar());
        paramsMap.put("oauth_timestamp", String.valueOf(getUnixTime()));
        paramsMap.put("oauth_token", REQUEST_TOKEN);
        paramsMap.put("oauth_signature_method", "HMAC-SHA1");
        paramsMap.put("oauth_version", "1.0");

        String sigData = makeSigData(CONSUMER_KEY, AUTHORIZE_URL, paramsMap, method);
        String sigKey = makeSigKey(CONSUMER_SECRET, REQUEST_TOKEN_SECRET);

        String redirectUrl = AUTHORIZE_URL + "?oauth_consumer_key=" + URLEncode((String) paramsMap.get("oauth_consumer_key")) + "&oauth_nonce=" + URLEncode((String) paramsMap.get("oauth_nonce")) + "&oauth_signature=" + URLEncode(makeSignature(sigKey, sigData)) + "&oauth_signature_method=" + URLEncode((String) paramsMap.get("oauth_signature_method")) + "&oauth_timestamp=" + URLEncode((String) paramsMap.get("oauth_timestamp")) + "&oauth_token=" + URLEncode((String) paramsMap.get("oauth_token")) + "&oauth_version=" + URLEncode((String) paramsMap.get("oauth_version"));
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
            str = URLEncoder.encode(str, "UTF-8");
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
}
