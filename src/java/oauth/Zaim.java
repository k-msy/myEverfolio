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
import java.util.regex.PatternSyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bpg0129
 */
public class Zaim extends SuperOauth {

    private static final String CONSUMER_KEY = "b12800bf82cfe709683c9b812d35fc450efb8bc4";
    private static final String CONSUMER_SECRET = "6a1b7a0ad40bdbd3d9b4d0a64fb757d10a606af4";
    private static String OAUTH_TOKEN = ""; // リクエストトークン取得時は利用しない
    private static String OAUTH_TOKEN_SECRET = ""; // リクエストトークン取得時は利用しない
    private static final String method = "POST";

    private static final String REQUEST_TOKEN_URL = "https://api.zaim.net/v2/auth/request";
    private static final String AUTHORIZE_URL = "https://auth.zaim.net/users/auth";
    private static final String ACCESS_TOKEN_URL = "https://api.zaim.net/v2/auth/access";

    private static String OAUTH_VERIFIER = "";

    private static final String OAUTH_CALLBACK = "http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true";
    private static final String TOKEN_SECRET = "";

    private String sigKey;
    private String sigData;

    SortedMap<String, String> paramsMap;
    private String authHeader;

    public Zaim() {

        this.paramsMap = super.makeParam(CONSUMER_KEY);
        this.sigData = super.makeSigData(CONSUMER_KEY, REQUEST_TOKEN_URL, paramsMap, method);
        this.sigKey = super.makeSigKey(CONSUMER_SECRET, OAUTH_TOKEN_SECRET);

    }

    public void verify(HttpServletRequest request, HttpServletResponse response) {

        //OAUTH_TOKENとOAUTH_TOKEN_SECRETを取得する
        String reqTokenResult = "";
        try {
            reqTokenResult = getRequestToken();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (reqTokenResult != "") {
            String[] split = extractToken(reqTokenResult);
            OAUTH_TOKEN = split[0];
            OAUTH_TOKEN_SECRET = split[1];
            //String url = AUTHORIZE_URL+"?oauth_token="+OAUTH_TOKEN;

            try {
                super.sendRedirect(request, response, AUTHORIZE_URL, OAUTH_TOKEN);
            } catch (IOException ex) {
                System.out.println("IO例外");
            }
        }else{
            Date date = new Date();
            System.out.println("zaimリクエストトークン取得失敗" + new Date().toString());
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
            if (e instanceof IOException) {
                e.printStackTrace();
                connection.getErrorStream();
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
}
