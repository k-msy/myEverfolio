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
import java.util.SortedMap;
import java.util.regex.PatternSyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bpg0129
 */
public class Twitter extends SuperOauth {

    // OAuthにおいて利用する変数宣言
    private static final String CONSUMER_KEY = "ZrWPQbG5ml4pRRRUgrpGJcvgK";
    private static final String CONSUMER_SECRET = "bfQnmsEO4eYmyUtLSP4HlY5sjQBvXxOvFKCjix2txf9uMg8DjW";
    private static String OAUTH_TOKEN = ""; // リクエストトークン取得時は利用しない
    private static String OAUTH_TOKEN_SECRET = ""; // リクエストトークン取得時は利用しない
    private static final String method = "POST";

    private static final String REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
    private static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
    private static final String ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";

    private static String OAUTH_VERIFIER = "";

    private static final String OAUTH_CALLBACK = "http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true";

    private String sigKey;
    private String sigData;

    SortedMap<String, String> paramsMap;
    private String authHeader;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("帰ってきたウルトラマン！？");
        OAUTH_VERIFIER = request.getParameter("oauth_verifier");

        String reqAccessTokenResult = getAccessToken();
        System.out.println("アクセストークン取れた！");
        System.out.println("reqAccessTokenResult : " + reqAccessTokenResult);
        String[] split = extractToken(reqAccessTokenResult);
        OAUTH_TOKEN = split[0];
        OAUTH_TOKEN_SECRET = split[1];

        tweet();
        //super.responseComplete();
    }

    public Twitter() {
        // OAuthにおいて利用する共通パラメーター
        // パラメーターはソートする必要があるためSortedMapを利用
        this.paramsMap = super.makeParam(CONSUMER_KEY);
        this.sigData = super.makeSigData(CONSUMER_KEY, REQUEST_TOKEN_URL, paramsMap, method);
        this.sigKey = super.makeSigKey(CONSUMER_SECRET, OAUTH_TOKEN_SECRET);

    }

    // APIにアクセス
    public void verify(HttpServletRequest request, HttpServletResponse response) {

        //OAUTH_TOKENとOAUTH_TOKEN_SECRETを取得する
        String reqTokenResult = "";
        try {
            reqTokenResult = getRequestToken();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String[] split = extractToken(reqTokenResult);
        OAUTH_TOKEN = split[0];
        OAUTH_TOKEN_SECRET = split[1];
        //String url = AUTHORIZE_URL+"?oauth_token="+OAUTH_TOKEN;

        try {
            super.sendRedirect(request, response, AUTHORIZE_URL, OAUTH_TOKEN);
        } catch (IOException ex) {
            System.out.println("IO例外");
        }

    }

    private String getRequestToken() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            // 署名をパラメータに追加
            this.paramsMap.put("oauth_signature", super.makeSignature(sigKey, sigData));

            // Authorizationヘッダの作成
            this.authHeader = super.makeAuthHeader(paramsMap);
            System.out.println("AuthorizationHeader :" + authHeader);

            URL url = new URL(REQUEST_TOKEN_URL);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", authHeader);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            if(e instanceof IOException){
                e.printStackTrace();
            }else{
                System.out.println("他のException");
            }
            
        } finally {
            if(reader != null){
                reader.close();
            }
            if(connection != null){
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

    private String getAccessToken() {

        try {
            this.paramsMap.clear();
            this.paramsMap = super.makeParam(CONSUMER_KEY);
            this.paramsMap.put("oauth_token", OAUTH_TOKEN);
            this.paramsMap.put("oauth_verifier", OAUTH_VERIFIER);

            this.sigData = super.makeSigData(CONSUMER_KEY, ACCESS_TOKEN_URL, paramsMap, method);
            this.sigKey = super.makeSigKey(CONSUMER_SECRET, OAUTH_TOKEN_SECRET);
            // 署名をパラメータに追加

            this.paramsMap.put("oauth_signature", super.makeSignature(sigKey, sigData));

            // Authorizationヘッダの作成
            this.authHeader = super.makeAuthHeader(paramsMap);
            System.out.println("AuthorizationHeader :" + authHeader);

            URL url = new URL(ACCESS_TOKEN_URL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", authHeader);
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return reader.readLine();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private void tweet() {
        try {
            this.paramsMap.clear();
            this.paramsMap = super.makeParam(CONSUMER_KEY);
            this.paramsMap.put("oauth_token", OAUTH_TOKEN);

            String status = "自前TwitterAPI連携からのつぶやき";
            paramsMap.put("status", URLEncode(status));

            this.sigData = super.makeSigData(CONSUMER_KEY, "https://api.twitter.com/1.1/statuses/update.json", paramsMap, method);
            this.sigKey = super.makeSigKey(CONSUMER_SECRET, OAUTH_TOKEN_SECRET);
            // 署名をパラメータに追加

            this.paramsMap.put("oauth_signature", super.makeSignature(sigKey, sigData));

            // (1)statusはAuthorizationヘッダーではなくurlに含めるためparamsから削除する
            paramsMap.remove("status");

            // Authorizationヘッダの作成
            this.authHeader = super.makeAuthHeader(paramsMap);
            System.out.println("AuthorizationHeader :" + authHeader);

            URL url = new URL("https://api.twitter.com/1.1/statuses/update.json?status=" + URLEncode(status));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", authHeader);
            connection.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            //return reader.readLine();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //return "";
    }

}
