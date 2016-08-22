package thirdparty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.SortedMap;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import oauth.SuperOauth;

public class Twitter
        extends SuperOauth
        implements Runnable {

    private static final String CONSUMER_KEY = "ZrWPQbG5ml4pRRRUgrpGJcvgK";
    private static final String CONSUMER_SECRET = "bfQnmsEO4eYmyUtLSP4HlY5sjQBvXxOvFKCjix2txf9uMg8DjW";
    private static String OAUTH_TOKEN = "";
    private static String OAUTH_TOKEN_SECRET = "";
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
    @Resource
    private ManagedExecutorService executeService = null;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        System.out.println("���������������������������������������");
        OAUTH_VERIFIER = request.getParameter("oauth_verifier");

        String reqAccessTokenResult = getAccessToken();
        System.out.println("������������������������������������");
        System.out.println("reqAccessTokenResult : " + reqAccessTokenResult);
        String[] split = extractToken(reqAccessTokenResult);
        OAUTH_TOKEN = split[0];
        OAUTH_TOKEN_SECRET = split[1];

        tweet();
    }

    public Twitter() {
        this.paramsMap = super.makeParam("ZrWPQbG5ml4pRRRUgrpGJcvgK", "http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true");
        this.sigData = super.makeSigData("ZrWPQbG5ml4pRRRUgrpGJcvgK", "https://api.twitter.com/oauth/request_token", this.paramsMap, "POST");
        this.sigKey = super.makeSigKey("bfQnmsEO4eYmyUtLSP4HlY5sjQBvXxOvFKCjix2txf9uMg8DjW", OAUTH_TOKEN_SECRET);
    }

    public void verify(HttpServletRequest request, HttpServletResponse response) {
        String reqTokenResult = "";
        try {
            reqTokenResult = getRequestToken();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String[] split = extractToken(reqTokenResult);
        OAUTH_TOKEN = split[0];
        OAUTH_TOKEN_SECRET = split[1];
        try {
            super.sendRedirect(response, "https://api.twitter.com/oauth/authorize", OAUTH_TOKEN);
        } catch (IOException ex) {
            System.out.println("IO������");
        }
    }

    private String getRequestToken()
            throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            this.paramsMap.put("oauth_signature", super.makeSignature(this.sigKey, this.sigData));

            this.authHeader = super.makeAuthHeader(this.paramsMap);
            System.out.println("AuthorizationHeader :" + this.authHeader);

            URL url = new URL("https://api.twitter.com/oauth/request_token");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", this.authHeader);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            if ((e instanceof IOException)) {
                e.printStackTrace();
            } else {
                System.out.println("������Exception");
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

    private String getAccessToken() {
        try {
            this.paramsMap.clear();
            this.paramsMap = super.makeParam("ZrWPQbG5ml4pRRRUgrpGJcvgK", "http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true");
            this.paramsMap.put("oauth_token", OAUTH_TOKEN);
            this.paramsMap.put("oauth_verifier", OAUTH_VERIFIER);

            this.sigData = super.makeSigData("ZrWPQbG5ml4pRRRUgrpGJcvgK", "https://api.twitter.com/oauth/access_token", this.paramsMap, "POST");
            this.sigKey = super.makeSigKey("bfQnmsEO4eYmyUtLSP4HlY5sjQBvXxOvFKCjix2txf9uMg8DjW", OAUTH_TOKEN_SECRET);

            this.paramsMap.put("oauth_signature", super.makeSignature(this.sigKey, this.sigData));

            this.authHeader = super.makeAuthHeader(this.paramsMap);
            System.out.println("AuthorizationHeader :" + this.authHeader);

            URL url = new URL("https://api.twitter.com/oauth/access_token");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", this.authHeader);
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
            this.paramsMap = super.makeParam("ZrWPQbG5ml4pRRRUgrpGJcvgK", "http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true");
            this.paramsMap.put("oauth_token", OAUTH_TOKEN);

            String status = "������TwitterAPI���������������������������";
            this.paramsMap.put("status", URLEncode(status));

            this.sigData = super.makeSigData("ZrWPQbG5ml4pRRRUgrpGJcvgK", "https://api.twitter.com/1.1/statuses/update.json", this.paramsMap, "POST");
            this.sigKey = super.makeSigKey("bfQnmsEO4eYmyUtLSP4HlY5sjQBvXxOvFKCjix2txf9uMg8DjW", OAUTH_TOKEN_SECRET);

            this.paramsMap.put("oauth_signature", super.makeSignature(this.sigKey, this.sigData));

            this.paramsMap.remove("status");

            this.authHeader = super.makeAuthHeader(this.paramsMap);
            System.out.println("AuthorizationHeader :" + this.authHeader);

            URL url = new URL("https://api.twitter.com/1.1/statuses/update.json?status=" + URLEncode(status));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", this.authHeader);
            connection.connect();
            BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
