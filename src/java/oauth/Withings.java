/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import entity.WithingsEnti;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 *
 * @author bpg0129
 */
@SessionScoped
public class Withings extends SuperOauth {

    // OAuthにおいて利用する変数宣言
    private static final String CONSUMER_KEY = "f1e9bebd38c1bf97b7c58bf2f5844c9bf7c38ec50254124d4f43b8582f0f";
    private static final String CONSUMER_SECRET = "c17484ff357d801897828f674bb6175b4f94340516ce5bb4922def7e035";
    private static final String REQUEST_TOKEN_URL = "https://oauth.withings.com/account/request_token";
    private static final String AUTHORIZE_URL = "https://oauth.withings.com/account/authorize";
    private static final String ACCESS_TOKEN_URL = "https://oauth.withings.com/account/access_token";
    private static final String OAUTH_CALLBACK = "http://127.0.0.1:8080/myEverfolio/faces/main/callback/top.xhtml?faces-redirect=true";
    private static final String method = "GET";

    HttpServletRequest request = getRequest();
    HttpSession session = request.getSession(true);

    private String sigKey;
    private String sigData;

    SortedMap<String, String> paramsMap;
    //private String authHeader;

    private int difference = 0;

    public Withings() {
        this.paramsMap = super.makeParam(CONSUMER_KEY);
        paramsMap.put("oauth_callback", URLEncode(OAUTH_CALLBACK));
        this.sigData = super.makeSigData(CONSUMER_KEY, REQUEST_TOKEN_URL, paramsMap, method);
        this.sigKey = super.makeSigKey(CONSUMER_SECRET, "");
    }

    /*
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        System.out.println("帰ってきたウルトラマン！？");
        session.setAttribute("userid", request.getParameter("userid"));
        session.setAttribute("oauth_token", request.getParameter("oauth_token"));
        session.setAttribute("oauth_verifier", request.getParameter("oauth_verifier"));

        getAccessToken();

        //昨日と今日の歩数を取得
        String twoDaysSteps = getTwoDaysSteps();
        System.out.println("stepsResult=" + twoDaysSteps);
        

        //昨日と今日の体重を取得
        String weightResult = getWeightMeasures();
        System.out.println("weightResult=" + weightResult);

        ChartView chart = new ChartView();
        //chart.init(stepList);

        //System.out.println("何個ある？" + testParse.length + "}");
        //System.out.println("testParse[0] + } =" + testParse[0] + "}");
        //String hogehoge = testParse[0] + "}";
        //System.out.println("hogehoge =" + hogehoge);
        //System.out.println("wi.steps = " + wi.getSteps());
        /*
        try {
            response.sendRedirect("http://127.0.0.1:8080/myEverfolio/faces/main/top.xhtml?faces-redirect=true");
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
        //super.responseComplete();
                */
    //}


    public void verify() {

        try {
            getRequestToken();
            sendRedirect();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void getRequestToken() throws IOException {
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
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String reqTokenResult = "";
            reqTokenResult = reader.readLine();
            if (!"".equals(reqTokenResult)) {
                String[] split = extractToken(reqTokenResult);
                session.setAttribute("request_token", split[0]);
                session.setAttribute("request_token_secret", split[1]);
            } else {
                Date date = new Date();
                System.out.println("withingsリクエストトークン取得失敗" + new Date().toString());
            }
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
    }

    
    private void sendRedirect() {
        //
        String REQUEST_TOKEN = session.getAttribute("request_token").toString();
        String REQUEST_TOKEN_SECRET = session.getAttribute("request_token_secret").toString();

        SortedMap<String, String> paramsMap = new TreeMap<>();
        paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
        paramsMap.put("oauth_nonce", super.getRandomChar());
        paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
        paramsMap.put("oauth_token", REQUEST_TOKEN);
        paramsMap.put("oauth_signature_method", "HMAC-SHA1");
        paramsMap.put("oauth_version", "1.0");

        this.sigData = super.makeSigData(CONSUMER_KEY, AUTHORIZE_URL, paramsMap, method);
        this.sigKey = super.makeSigKey(CONSUMER_SECRET, REQUEST_TOKEN_SECRET);

        String redirectUrl = AUTHORIZE_URL
                + "?oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData))
                + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                + "&oauth_token=" + URLEncode(paramsMap.get("oauth_token"))
                + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version"));

        try {
            HttpServletResponse response = getResponse();
            response.sendRedirect(redirectUrl);
        } catch (IOException ex) {
            Logger.getLogger(Withings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getAccessToken() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String OAUTH_TOKEN = session.getAttribute("oauth_token").toString();
        String REQEST_TOKEN_SECRET = session.getAttribute("request_token_secret").toString();
        try {
            this.paramsMap.clear();

            paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", OAUTH_TOKEN);
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_version", "1.0");

            this.sigData = super.makeSigData(CONSUMER_KEY, ACCESS_TOKEN_URL, paramsMap, method);
            this.sigKey = super.makeSigKey(CONSUMER_SECRET, REQEST_TOKEN_SECRET);

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
            String[] split = extractToken(reader.readLine());
            System.out.println("access_token=" + split[0]);
            System.out.println("access_token_secret=" + split[1]);
            session.setAttribute("access_token", split[0]);
            session.setAttribute("access_token_secret", split[1]);
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

    public String getRawDataForSteps() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String ACCESS_TOKEN = session.getAttribute("access_token").toString();
        String ACCESS_TOKEN_SECRET = session.getAttribute("access_token_secret").toString();
        String USER_ID = session.getAttribute("userid").toString();

        try {
            this.paramsMap.clear();

            paramsMap.put("action", "getactivity");

            Map<String, String> dateMap = new HashMap();
            dateMap = getTodayAndYesterday(dateMap);
            paramsMap.put("startdateymd", dateMap.get("yesterday"));
            paramsMap.put("enddateymd", dateMap.get("today"));
            paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", ACCESS_TOKEN);
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_version", "1.0");
            paramsMap.put("userid", USER_ID);

            this.sigData = super.makeSigData(CONSUMER_KEY, "https://wbsapi.withings.net/v2/measure", paramsMap, method);
            //System.out.println("this.sigData=" + this.sigData);
            this.sigKey = super.makeSigKey(CONSUMER_SECRET, ACCESS_TOKEN_SECRET);

            URL url = new URL("https://wbsapi.withings.net/v2/measure"
                    + "?action=" + "getactivity"
                    + "&userid=" + URLEncode(USER_ID)
                    + "&startdateymd=" + URLEncode(paramsMap.get("startdateymd"))
                    + "&enddateymd=" + URLEncode(paramsMap.get("enddateymd"))
                    + "&oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                    + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                    + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData))
                    + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                    + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                    + "&oauth_token=" + URLEncode(ACCESS_TOKEN)
                    + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version")));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String text = reader.readLine();
            //System.out.println("text=" + text);
            String[] result = null;
            result = text.split("\\[");
            String[] jsonText = result[1].split("\\]");
            System.out.println("jsonText[0]=" + jsonText[0]);

            return jsonText[0];
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

    public String getWeightMeasures() throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        String USER_ID = session.getAttribute("userid").toString();
        String ACCESS_TOKEN = session.getAttribute("access_token").toString();
        String ACCESS_TOKEN_SECRET = session.getAttribute("access_token_secret").toString();
        try {
            this.paramsMap.clear();

            paramsMap.put("action", "getmeas");
            paramsMap.put("userid", USER_ID);
            Map<String, String> utcMap = new HashMap();
            utcMap = getUTC_TodayAndYesterday(utcMap);
            System.out.println("yesterday=" + utcMap.get("yesterday"));
            System.out.println("today=" + utcMap.get("today"));
            paramsMap.put("startdate", utcMap.get("yesterday"));
            paramsMap.put("enddate", utcMap.get("today"));
            paramsMap.put("meastype", "1");
            paramsMap.put("oauth_consumer_key", CONSUMER_KEY);
            paramsMap.put("oauth_nonce", super.getRandomChar());
            paramsMap.put("oauth_signature_method", "HMAC-SHA1");
            paramsMap.put("oauth_timestamp", String.valueOf(super.getUnixTime()));
            paramsMap.put("oauth_token", ACCESS_TOKEN);
            paramsMap.put("oauth_version", "1.0");

            this.sigData = super.makeSigData(CONSUMER_KEY, "https://wbsapi.withings.net/measure", paramsMap, method);
            //System.out.println("this.sigData=" + this.sigData);
            this.sigKey = super.makeSigKey(CONSUMER_SECRET, ACCESS_TOKEN_SECRET);

            URL url = new URL("https://wbsapi.withings.net/measure"
                    + "?action=" + "getmeas"
                    + "&userid=" + URLEncode(USER_ID)
                    + "&startdate=" + URLEncode(paramsMap.get("startdate"))
                    + "&enddate=" + URLEncode(paramsMap.get("enddate"))
                    + "&meastype=" + URLEncode(paramsMap.get("meastype"))
                    + "&oauth_consumer_key=" + URLEncode(paramsMap.get("oauth_consumer_key"))
                    + "&oauth_nonce=" + URLEncode(paramsMap.get("oauth_nonce"))
                    + "&oauth_signature=" + URLEncode(super.makeSignature(sigKey, sigData))
                    + "&oauth_signature_method=" + URLEncode(paramsMap.get("oauth_signature_method"))
                    + "&oauth_timestamp=" + URLEncode(paramsMap.get("oauth_timestamp"))
                    + "&oauth_token=" + URLEncode(ACCESS_TOKEN)
                    + "&oauth_version=" + URLEncode(paramsMap.get("oauth_version")));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String text = reader.readLine();
            //System.out.println("weightResultRowResult=" + text);

            return text;
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

    private Map<String, String> getTodayAndYesterday(Map<String, String> dateMap) {
        //今日の日付を取得、yyyy-MM-ddに変換
        Date date = new Date();
        SimpleDateFormat formatted = new SimpleDateFormat("yyyy-MM-dd");
        dateMap.put("today", formatted.format(date));

        //昨日の日付を取得、yyyy-MM-ddに変換
        Calendar yesterday = Calendar.getInstance();
        yesterday.setTime(date);
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        dateMap.put("yesterday", formatted.format(yesterday.getTime()));
        return dateMap;
    }

    private Map<String, String> getUTC_TodayAndYesterday(Map<String, String> utcMap) {
        Long nowTime = System.currentTimeMillis() / 1000L;
        //Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        //今日の日付を取得
        utcMap.put("today", nowTime.toString());
        //昨日の日付を取得
        Long pastTime = nowTime - 86400;
        utcMap.put("yesterday", pastTime.toString());

        return utcMap;
    }



    public Map<String, String> adjustSteps(String rawDataForSteps) throws IOException {
        //任意の期間中のJSONデータをListに詰め替える
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<WithingsEnti> wiList = new ArrayList<WithingsEnti>();
        //「,」で区切られたresultの1つを試しにパースできるかどうか確認する
        String[] testParse = rawDataForSteps.split("},");
        for (int i = 0; i < testParse.length; i++) {
            WithingsEnti wi = mapper.readValue(testParse[i] + "}", WithingsEnti.class);
            wiList.add(wi);
        }
        //Listから「歩数」のみ抽出して、Listに詰め替える
        ArrayList<Integer> stepList = new ArrayList<Integer>();
        for (int i = 0; i < wiList.size(); i++) {
            stepList.add(wiList.get(i).getSteps());
            System.out.println(String.valueOf(i) + "番目のステップ数 =" + wiList.get(i).getSteps());
        }
        System.out.println("ステップリスト内の要素数：" + String.valueOf(stepList.size()));
        
        Map<String, String> stepsMap = new HashMap<>();
        if (1 >= stepList.size()) {
            //1日分しか歩数データが取れなかった場合
            System.out.println("今日のデータが同期されてないっぽいです");
        } else {
            //2日分歩数データが取れた場合
            int yesterday=stepList.get(0);
            int today=stepList.get(1);
            stepsMap.put("yesterday", String.valueOf(yesterday));
            stepsMap.put("today", String.valueOf(today));
            stepsMap.put("difference", String.valueOf(today - yesterday));
            System.out.println("today - yesterday=" + String.valueOf(today - yesterday));
        }  
        return stepsMap;
    }

}
