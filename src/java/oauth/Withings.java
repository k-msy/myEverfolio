/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bpg0129
 */
public class Withings extends SuperOauth {

    // OAuthにおいて利用する変数宣言
    private static final String CONSUMER_KEY = "f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f";
    private static final String CONSUMER_SECRET = "c17484ff357d801897828f674bb6175b4f94340516ce5bb4922def7e035";
    private static final String REQUEST_TOKEN_URL = "https://oauth.withings.com/account/request_token";
    private static final String AUTHORIZE_URL = "https://oauth.withings.com/account/authorize";
    private static final String ACCESS_TOKEN_URL = "https://oauth.withings.com/account/access_token";
    private static final String OAUTH_CALLBACK = "http://127.0.0.1:8080/myEverfolio/faces/main/callback/top.xhtml?faces-redirect=true";
    private static final String method = "GET";

    private static String OAUTH_TOKEN = ""; // リクエストトークン取得時は利用しない
    private static String OAUTH_TOKEN_SECRET = ""; // リクエストトークン取得時は利用しない
    private static String OAUTH_VERIFIER = "";
    private static String userid = "";

    private String sigKey;
    private String sigData;

    SortedMap<String, String> paramsMap;
    private String authHeader;

    public Withings() {
        /*
        OAUTH_TOKEN = ""; // リクエストトークン取得時は利用しない
        OAUTH_TOKEN_SECRET = ""; // リクエストトークン取得時は利用しない
        OAUTH_VERIFIER = "";
        userid = "";
                */
        this.paramsMap = super.makeParam(CONSUMER_KEY);
        paramsMap.put("oauth_callback", URLEncode(OAUTH_CALLBACK));
        this.sigData = super.makeSigData(CONSUMER_KEY, REQUEST_TOKEN_URL, paramsMap, method);
        this.sigKey = super.makeSigKey(CONSUMER_SECRET, OAUTH_TOKEN_SECRET);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("帰ってきたウルトラマン！？");
        userid = request.getParameter("userid");
        OAUTH_TOKEN = request.getParameter("oauth_token");
        OAUTH_VERIFIER = request.getParameter("oauth_verifier");

        String reqAccessTokenResult = getAccessToken();
        System.out.println("アクセストークン取れた！");
        System.out.println("reqAccessTokenResult : " + reqAccessTokenResult);
        String[] split = extractToken(reqAccessTokenResult);
        OAUTH_TOKEN = split[0];
        OAUTH_TOKEN_SECRET = split[1];

        String result = getActivityMeasures();
        System.out.println("result=" + result);

        try {
            response.sendRedirect("http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true");
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
        //super.responseComplete();
    }

    public void verify(HttpServletRequest request, HttpServletResponse response) {

        //OAUTH_TOKENとOAUTH_TOKEN_SECRETを取得する
        String reqTokenResult = "";
        try {
            reqTokenResult = getRequestToken();
            System.out.println("reqTokenResult: " + reqTokenResult);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (reqTokenResult != "") {
            String[] split = extractToken(reqTokenResult);
            OAUTH_TOKEN = split[0];
            OAUTH_TOKEN_SECRET = split[1];
            //String url = AUTHORIZE_URL+"?oauth_token="+OAUTH_TOKEN;
            this.sendRedirect(request, response);
        } else {
            Date date = new Date();
            System.out.println("withingsリクエストトークン取得失敗" + new Date().toString());
        }

    }

    private String getRequestToken() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            // 署名をパラメータに追加
            this.paramsMap.put("oauth_signature", super.makeSignature(sigKey, sigData));
            
            URL url = new URL(REQUEST_TOKEN_URL
                    + "?oauth_callback=" + URLEncode(OAUTH_CALLBACK)
                    + "&oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                    + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                    + "&oauth_signature=" + URLEncode(paramsMap.get("oauth_signature"))
                    + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                    + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                    + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version")));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            //connection.setRequestProperty("Authorization", authHeader);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            if (e instanceof IOException) {
                e.printStackTrace();
            } else {
                System.out.println("他のException");
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

    private String[] extractToken(String chars) {
        String[] result = null;
        try {
            result = chars.split("&");
            for (int i = 0; i < result.length; i++) {
                String[] a = result[i].split("=");
                result[i] = a[1];
            }
        } catch (PatternSyntaxException ex) {
            System.out.println("のっとぱたーんまっち");
        }
        if (result == null || result.length == 0) {
            result[0] = "";
        }
        return result;
    }

    private String getAccessToken() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            this.paramsMap.clear();

            paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", OAUTH_TOKEN);
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_version", "1.0");

            this.sigData = super.makeSigData(CONSUMER_KEY, ACCESS_TOKEN_URL, paramsMap, method);
            this.sigKey = super.makeSigKey(CONSUMER_SECRET, OAUTH_TOKEN_SECRET);

            // 署名をパラメータに追加
            this.paramsMap.put("oauth_signature", super.makeSignature(sigKey, sigData));

            URL url = new URL(ACCESS_TOKEN_URL
                    + "?oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                    + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                    + "&oauth_signature=" + URLEncode(paramsMap.get("oauth_signature"))
                    + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                    + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                    + "&oauth_token=" + URLEncode(OAUTH_TOKEN)
                    + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version")));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return reader.readLine();
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
        return "";
    }

    //リクエストトークンを取得したら、次はユーザーにWithingsの認証をしてもらう
    private void sendRedirect(HttpServletRequest request, HttpServletResponse response) {
        //
        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
        paramsMap.put("oauth_nonce", super.getRandomChar());
        paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
        paramsMap.put("oauth_token", OAUTH_TOKEN);
        paramsMap.put("oauth_signature_method", "HMAC-SHA1");
        paramsMap.put("oauth_version", "1.0");

        this.sigData = super.makeSigData(CONSUMER_KEY, AUTHORIZE_URL, paramsMap, method);
        this.sigKey = super.makeSigKey(CONSUMER_SECRET, OAUTH_TOKEN_SECRET);

        String redirectUrl = AUTHORIZE_URL
                + "?oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData))
                + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                + "&oauth_token=" + URLEncode(paramsMap.get("oauth_token"))
                + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version"));

        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getActivityMeasures() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            this.paramsMap.clear();

            paramsMap.put("action", "getactivity");
            
            //paramsMap.put("date", "2015-11-19");
            paramsMap.put("startdateymd", "2015-11-01");
            paramsMap.put("enddateymd", "2015-11-19");
            paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", OAUTH_TOKEN);
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_version", "1.0");
            paramsMap.put("userid", userid);

            this.sigData = super.makeSigData(CONSUMER_KEY, "https://wbsapi.withings.net/v2/measure", paramsMap, method);
            System.out.println("this.sigData=" + this.sigData);
            this.sigKey = super.makeSigKey(CONSUMER_SECRET, OAUTH_TOKEN_SECRET);

            URL url = new URL("https://wbsapi.withings.net/v2/measure"
                    + "?action=" + "getactivity"
                    + "&userid=" + URLEncode(userid)
                    //+ "&date=" + URLEncode("2015-11-19")
                    + "&startdateymd=" + URLEncode(paramsMap.get("startdateymd"))
                    + "&enddateymd=" + URLEncode(paramsMap.get("enddateymd"))
                    + "&oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                    + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                    + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData))
                    + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                    + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                    + "&oauth_token=" + URLEncode(OAUTH_TOKEN)
                    + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version")));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return reader.readLine();
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
        return "";
    }

}
