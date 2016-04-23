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
import java.util.Map;
import java.util.SortedMap;
import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.vvakame.util.jsonpullparser.JsonFormatException;
import net.vvakame.zaim4j.OAuthConfiguration;
import net.vvakame.zaim4j.OAuthCredential;
import net.vvakame.zaim4j.Zaim;

/**
 *
 * @author bpg0129
 */
@SessionScoped
public class Zaim_origin extends SuperOauth {

    private static final String CONSUMER_KEY = "b12800bf82cfe709683c9b812d35fc450efb8bc4";
    private static final String CONSUMER_SECRET = "6a1b7a0ad40bdbd3d9b4d0a64fb757d10a606af4";
    
    private static String OAUTH_TOKEN = ""; // リクエストトークン取得時は利用しない
    private static String OAUTH_TOKEN_SECRET = ""; // リクエストトークン取得時は利用しない

    private static final String CALLBACK_URL = "http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml";

    private static final String REQUEST_TOKEN_URL = "https://api.zaim.net/v2/auth/request";
    private static final String AUTHORIZE_URL = "https://auth.zaim.net/users/auth";
    private static final String ACCESS_TOKEN_URL = "https://api.zaim.net/v2/auth/access";

    private static final String OAUTH_CALLBACK = "http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true";
    private static final String TOKEN_SECRET = "";
    
    private static final String method = "POST";
    
    private String sigKey;
    private String sigData;

    SortedMap<String, String> paramsMap;
    private String authHeader;

    public Zaim_origin() {
        // OAuthにおいて利用する共通パラメーター
        // パラメーターはソートする必要があるためSortedMapを利用
        this.paramsMap = super.makeParam(CONSUMER_KEY);
        this.sigData = super.makeSigData(CONSUMER_KEY, REQUEST_TOKEN_URL, paramsMap, method);
        this.sigKey = super.makeSigKey(CONSUMER_SECRET, OAUTH_TOKEN_SECRET);
    }

    public void verify() throws IOException {
        
                HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            // 署名をパラメータに追加
            this.paramsMap.put("oauth_signature", super.makeSignature(sigKey, sigData));

            // Authorizationヘッダの作成
            this.authHeader = super.makeAuthHeader(paramsMap);
            System.out.println("AuthorizationHeader :" + authHeader);
            String sURL = REQUEST_TOKEN_URL + "?" + getParameter(paramsMap) + "&oauth_callback=" + URLEncode(CALLBACK_URL);
            URL url = new URL(sURL);

            connection = (HttpURLConnection) url.openConnection();
/*
            connection.setRequestMethod(method);
            connection.setRequestProperty("Authorization", authHeader);
*/
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            System.out.println("reader.toString() =" + reader.toString());
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
        
        
        
        
        
        
        
        
/*
        // setup OAuthConfiguration
        OAuthConfiguration.Builder builder = OAuthConfiguration.Builder.newBuilder();
        builder.setConsumerKey(CONSUMER_KEY);
        builder.setConsumerSecret(CONSUMER_SECRET);
        builder.setCallbackUrl(CALLBACK_URL);
        OAuthConfiguration configuration = builder.build();

        OAuthCredential credential;
        // get RequestToken
        credential = Zaim.getRequestToken(configuration);
        // autorization url for User, please open by browser.
        String authUrl = credential.getAuthUrl();
        System.out.println("open: " + authUrl);
        System.out.print("input oauthVerifier: ");
        String oauthVerifier = new BufferedReader(new InputStreamReader(System.in)).readLine();

        // get AccessToken
        credential = Zaim.getAccessToken(configuration, credential, oauthVerifier);

        // Mission completed!
        System.out.println("credential.toJson()=" + credential.toJson()); // → OAUTH_TOKEN_JSON
        System.out.println("credential.getOauthToken()=" + credential.getOauthToken());
        System.out.println("credential.getOauthTokenSecret()=" + credential.getOauthTokenSecret());
*/
    }

    private String getParameter(SortedMap<String, String> paramsMap) {
        String paramStr = "X";
        for (Map.Entry<String, String> param : paramsMap.entrySet()) {
            paramStr += "&" + param.getKey() + "="
                    + URLEncode(param.getValue());
        }
        return paramStr = paramStr.substring(2);
    }

}
